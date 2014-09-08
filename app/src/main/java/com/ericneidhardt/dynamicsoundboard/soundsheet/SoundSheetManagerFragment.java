package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.customview.SlidingTabLayout;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromIntent;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.List;

/**
 * Created by Eric Neidhardt on 29.08.2014.
 */
public class SoundSheetManagerFragment extends Fragment implements View.OnClickListener, SoundSheetAdapter.OnItemClickListener, SoundSheetAdapter.OnItemDeleteListener, ActionbarEditText.OnTextEditedListener
{
	public static final String TAG = SoundSheetManagerFragment.class.getSimpleName();

	private static final String DB_SOUND_SHEETS = "com.ericneidhardt.dynamicsoundboard.db_sound_sheets";

	private SoundSheetAdapter soundSheetAdapter;
	private DaoSession daoSession;
	private TabContentAdapter tabContentAdapter;

	private Dialog pendingDialog;
	private boolean hasPendingLoadSoundSheetsTask = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUND_SHEETS);
		this.soundSheetAdapter = new SoundSheetAdapter();
		this.soundSheetAdapter.setOnItemClickListener(this);
		this.soundSheetAdapter.setOnItemDeleteListener(this);
		this.tabContentAdapter = new TabContentAdapter();

		LoadSoundSheetsTask task = new LoadSoundSheetsTask();
		task.execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.buildNavigationDrawerTabLayout();

		this.getActivity().findViewById(R.id.action_add_sound_sheet).setOnClickListener(this);
		((ActionbarEditText)this.getActivity().findViewById(R.id.et_set_label)).setOnTextEditedListener(this);
	}

	private void buildNavigationDrawerTabLayout()
	{
		ViewPager tabContent = (ViewPager) this.getActivity().findViewById(R.id.vp_tab_content);
		tabContent.setAdapter(this.tabContentAdapter);

		SlidingTabLayout tabBar = (SlidingTabLayout) this.getActivity().findViewById(R.id.layout_tab);
		tabBar.setViewPager(tabContent);
		tabBar.setCustomTabColorizer(new SlidingTabLayout.TabColorizer()
		{
			@Override
			public int getIndicatorColor(int position)
			{
				if (position == 0)
					return getResources().getColor(R.color.accent_200);
				else
					return getResources().getColor(R.color.primary_500);
			}

			@Override
			public int getDividerColor(int position)
			{
				return 0;
			}
		});

		RecyclerView listSoundSheets = (RecyclerView)this.getActivity().findViewById(R.id.rv_sound_sheets);
		listSoundSheets.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST));
		listSoundSheets.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		listSoundSheets.setItemAnimator(new DefaultItemAnimator());
		listSoundSheets.setAdapter(this.soundSheetAdapter);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (this.pendingDialog != null && this.pendingDialog.isShowing())
			this.pendingDialog.dismiss();
		StoreSoundSheetsTask task = new StoreSoundSheetsTask(this.soundSheetAdapter.getValues());
		task.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_clear_sound_sheets:
				((BaseActivity)this.getActivity()).removeSoundFragment(this.soundSheetAdapter.getValues());
				this.soundSheetAdapter.clear();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound_sheet:
				this.openDialogAddNewSoundLayout();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.getActivity() != null)
		{
			this.soundSheetAdapter.setSelectedItem(position);
			this.openSoundSheetFragment(data);
		}
	}

	@Override
	public void onItemDelete(View view, SoundSheet data, int position)
	{
		this.soundSheetAdapter.remove(data);
		if (this.getActivity() != null)
		{
			((BaseActivity)this.getActivity()).removeSoundFragment(data);

			if (data.getIsSelected())
			{
				List<SoundSheet> soundSheets = this.soundSheetAdapter.getValues();
				if (soundSheets.size() > 0)
				{
					int positionOfNewSelectedSoundSheet = (position > 0) ? position - 1 : 0;
					this.soundSheetAdapter.setSelectedItem(positionOfNewSelectedSoundSheet);
					this.openSoundSheetFragment(soundSheets.get(positionOfNewSelectedSoundSheet));
				}
			}
		}
	}

	@Override
	public void onTextEdited(String text)
	{
		SoundSheet currentActiveSoundSheet = this.soundSheetAdapter.getSelectedItem();
		if (currentActiveSoundSheet == null)
			throw new NullPointerException("sound sheet label was edited, but no sound sheet is selected");

		currentActiveSoundSheet.setLabel(text);
		this.soundSheetAdapter.notifyDataSetChanged();
	}

	private void handleIntent(Intent intent, boolean fromLoadingTask)
	{
		if (intent == null)
			return;

		if (this.hasPendingLoadSoundSheetsTask && !fromLoadingTask) // data loading must be completed, before intents can be processed
			return;

		if (intent.getAction().equals(Intent.ACTION_VIEW)
				&& intent.getData() != null)
		{
			this.openDialogAddNewSoundFromIntent(intent.getData());
			this.getActivity().setIntent(null);
		}
	}

	private void openDialogAddNewSoundLayout()
	{
		if (this.pendingDialog != null && this.pendingDialog.isShowing())
			this.pendingDialog.dismiss();
		this.pendingDialog = AddNewSoundSheetDialog.create(this.getActivity(), this.getSuggestedSoundSheetName(), new AddNewSoundSheetDialog.OnAddSoundSheetListener()
		{
			@Override
			public void onAddSoundSheet(String label)
			{
				soundSheetAdapter.add(getNewSoundSheet(label));
			}
		});
		this.pendingDialog.show();
	}

	private void openDialogAddNewSoundFromIntent(final Uri soundUri)
	{
		AddNewSoundFromIntent.OnAddSoundFromIntentListener listener = new AddNewSoundFromIntent.OnAddSoundFromIntentListener()
		{
			@Override
			public void onAddSoundFromIntent(String soundLabel, String newSoundSheetName, SoundSheet existingSoundSheet)
			{
				// TODO init MediaPlayer jus for storage is bad for performance
				EnhancedMediaPlayer player = new EnhancedMediaPlayer(soundUri, soundLabel);
				if (newSoundSheetName != null)
				{
					SoundSheet newCreatedSoundSheet = getNewSoundSheet(newSoundSheetName);
					soundSheetAdapter.add(newCreatedSoundSheet);
					// TODO add sound to SoundSheet
				}
				else if (existingSoundSheet != null)
				{
					Fragment addSoundToThisFragment = getActivity().getFragmentManager().findFragmentByTag(existingSoundSheet.getFragmentTag());
					if (addSoundToThisFragment == null)
						DynamicSoundboardApplication.storeSoundInDatabase(existingSoundSheet.getFragmentTag(), player);
					else
						((SoundSheetFragment)addSoundToThisFragment).addMediaPlayer(player);
				}
			}
		};
		if (this.pendingDialog != null && this.pendingDialog.isShowing())
			this.pendingDialog.dismiss();

		if (this.soundSheetAdapter.getItemCount() == 0)
			this.pendingDialog = AddNewSoundFromIntent.create(this.getActivity(), soundUri, this.getSuggestedSoundSheetName(), listener);
		else
			this.pendingDialog = AddNewSoundFromIntent.create(this.getActivity(), soundUri, this.getSuggestedSoundSheetName(), this.soundSheetAdapter.getValues(), listener);

		this.pendingDialog.show();
	}

	private String getSuggestedSoundSheetName()
	{
		return this.getActivity().getResources().getString(R.string.suggested_sound_sheet_name) + this.soundSheetAdapter.getItemCount();
	}

	private void openSoundSheetFragment(SoundSheet soundSheet)
	{
		BaseActivity activity = (BaseActivity)this.getActivity();
		activity.closeNavigationDrawer();
		activity.openSoundFragment(soundSheet);
		ActionbarEditText soundSheetLabel = (ActionbarEditText)activity.findViewById(R.id.et_set_label);
		soundSheetLabel.setText(soundSheet.getLabel());
	}

	private SoundSheet getNewSoundSheet(String label)
	{
		String tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
		return new SoundSheet(null, tag, label, false);
	}

	private class TabContentAdapter extends PagerAdapter
	{
		@Override
		public CharSequence getPageTitle(int position)
		{
			if (position == 0)
				return getResources().getString(R.string.tab_sound_sheets);
			else
				return getResources().getString(R.string.tab_play_list);
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			int resId = 0;
			switch (position) {
				case 0:
					resId = R.id.rv_sound_sheets;
					break;
				case 1:
					resId = R.id.lv_playlist;
					break;
			}
			return getActivity().findViewById(resId);
		}
	}

	private class LoadSoundSheetsTask extends SafeAsyncTask<List<SoundSheet>>
	{
		public LoadSoundSheetsTask()
		{
			hasPendingLoadSoundSheetsTask = true;
		}

		@Override
		public List<SoundSheet> call() throws Exception
		{
			return daoSession.getSoundSheetDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<SoundSheet> soundSheets) throws Exception
		{
			super.onSuccess(soundSheets);

			if (soundSheets.size() > 0)
			{
				soundSheetAdapter.addAll(soundSheets);
				SoundSheet currentActiveSoundSheet = soundSheetAdapter.getSelectedItem();
				if (currentActiveSoundSheet != null)
				{
					int indexSelectedItem = soundSheetAdapter.getValues().indexOf(currentActiveSoundSheet); // make sure only one item is selected on startup
					soundSheetAdapter.setSelectedItem(indexSelectedItem); // set selection for this item and remove all other selections
					openSoundSheetFragment(currentActiveSoundSheet);
				}
			}

			handleIntent(getActivity().getIntent(), true); //check for intent to handle, they require that all sound sheets are loaded
			hasPendingLoadSoundSheetsTask = false;
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			hasPendingLoadSoundSheetsTask = false;
			throw new RuntimeException(e);
		}
	}

	private class StoreSoundSheetsTask extends SafeAsyncTask<Void>
	{
		private List<SoundSheet> soundSheets;

		private StoreSoundSheetsTask(List<SoundSheet> soundSheets)
		{
			this.soundSheets = soundSheets;
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getSoundSheetDao().deleteAll();
			daoSession.getSoundSheetDao().insertInTx(soundSheets);
			return null;
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
