package org.neidhardt.dynamicsoundboard.soundsheetmanagement;


import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.edittext.ActionbarEditText;
import org.neidhardt.dynamicsoundboard.customview.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog.ConfirmDeleteAllSoundSheetsDialog;
import org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog.ConfirmDeleteSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.events.SoundLoadedEvent;
import org.neidhardt.dynamicsoundboard.events.SoundSheetsLoadedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.LoadSoundSheetsTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.RemoveSoundSheetTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.StoreSoundSheetTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.UpdateSoundSheetsTask;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetsManagerFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			CustomEditText.OnTextEditedListener
{
	public static final String TAG = SoundSheetsManagerFragment.class.getName();

	private static final String DB_SOUND_SHEETS_DEFAULT = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets";
	private static final String DB_SOUND_SHEETS = "db_sound_sheets";

	private List<SoundSheet> soundSheets;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.initSoundSheets();
	}

	public void initSoundSheets()
	{
		this.soundSheets = new ArrayList<>();
		this.daoSession = Util.setupDatabase(this.getActivity(), this.getDatabaseName());

		SafeAsyncTask task = new LoadSoundSheetsTask(this.daoSession);
		task.execute();
	}

	private String getDatabaseName()
	{
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		if (baseName.equals(SoundLayoutsManager.DB_DEFAULT))
			return DB_SOUND_SHEETS_DEFAULT;
		return baseName + DB_SOUND_SHEETS;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.getActivity().findViewById(R.id.action_add_sound_sheet).setOnClickListener(this);

		ActionbarEditText labelCurrentSoundSheet = (ActionbarEditText)this.getActivity().findViewById(R.id.et_set_label);
		labelCurrentSoundSheet.setOnTextEditedListener(this);
		SoundSheet currentActiveSoundSheet = this.getSoundSheetForCurrentFragment();
		if (currentActiveSoundSheet != null)
			labelCurrentSoundSheet.setText(currentActiveSoundSheet.getLabel());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EventBus.getDefault().registerSticky(this, 1);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		SafeAsyncTask task = new UpdateSoundSheetsTask(this.daoSession, this.soundSheets);
		task.execute();
	}

	@Override
	public void onStop()
	{
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	public void storeSoundSheets()
	{
		SafeAsyncTask task = new UpdateSoundSheetsTask(this.daoSession, this.soundSheets);
		task.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_delete_sheet:
				ConfirmDeleteSoundSheetDialog.showInstance(this.getFragmentManager());
				return true;
			case R.id.action_clear_sound_sheets:
				ConfirmDeleteAllSoundSheetsDialog.showInstance(this.getFragmentManager());
				return true;
			default:
				return false;
		}
	}

	public void deleteCurrentActiveSoundSheet()
	{
		SoundSheetFragment fragment = BaseActivity.getCurrentFragment(this.getFragmentManager());
		if (fragment != null)
		{
			fragment.removeAllSounds();
			SoundSheet soundSheet = this.get(fragment.getFragmentTag());
			this.remove(fragment.getFragmentTag());
			this.getBaseActivity().removeSoundFragment(soundSheet);
		}
	}

	public void deleteAllSoundSheets()
	{
		BaseActivity activity = (BaseActivity) this.getActivity();
		activity.removeSoundFragment(this.soundSheets);
		activity.setSoundSheetActionsEnable(false);

		ServiceManagerFragment fragment = this.getServiceManagerFragment();
		MusicService service = fragment.getSoundService();
		for (SoundSheet soundSheet : this.soundSheets)
		{
			List<EnhancedMediaPlayer> soundsInSoundSheet = fragment.getSounds().get(soundSheet.getFragmentTag());
			service.removeSounds(soundsInSoundSheet);
		}
		this.soundSheets.clear();
		this.daoSession.getSoundSheetDao().deleteAll();

		fragment.notifyPlaylist();

		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged();
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound_sheet:
				this.openDialogAddNewSoundSheet();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	public void openDialogAddNewSoundSheet()
	{
		AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), this.getSuggestedSoundSheetName());
	}

	@Override
	public void onTextEdited(String text)
	{
		FragmentManager manager = this.getFragmentManager();
		if (manager == null)
			return;

		SoundSheetFragment currentSoundSheetFragment = BaseActivity.getCurrentFragment(manager);
		if (currentSoundSheetFragment == null)
			return;

		SoundSheet correspondingSoundSheetData = this.get(currentSoundSheetFragment.getFragmentTag());
		if (correspondingSoundSheetData == null)
			throw new NullPointerException("sound sheet label was edited, but no sound sheet is selected");

		correspondingSoundSheetData.setLabel(text);
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged();
	}

	public SoundSheet get(String soundSheetTag)
	{
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getFragmentTag().equals(soundSheetTag))
				return soundSheet;
		}
		return null;
	}

	public void remove(SoundSheet soundSheet, boolean notifySoundSheets)
	{
		this.soundSheets.remove(soundSheet);
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();

		if (notifySoundSheets)
			navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged();

		SafeAsyncTask task = new RemoveSoundSheetTask(this.daoSession, soundSheet);
		task.execute();
	}

	public void remove(String fragmentTag)
	{
		SoundSheet soundSheetToRemove = null;
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getFragmentTag().equals(fragmentTag))
				soundSheetToRemove = soundSheet;
		}
		if (soundSheetToRemove != null)
			this.remove(soundSheetToRemove, true);
	}

	public List<SoundSheet> getSoundSheets()
	{
		return this.soundSheets;
	}

	public SoundSheet getSoundSheetForCurrentFragment()
	{
		SoundSheetFragment currentSoundSheetFragment = BaseActivity.getCurrentFragment(this.getFragmentManager());
		return currentSoundSheetFragment != null ? this.get(currentSoundSheetFragment.getFragmentTag()) : null;
	}

	public void addSoundSheetAndNotifyFragment(SoundSheet soundSheet)
	{
		this.soundSheets.add(soundSheet);
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged();

		SafeAsyncTask task = new StoreSoundSheetTask(this.daoSession, soundSheet);
		task.execute();
	}

	public void addSoundToSoundSheet(Uri soundUri, String soundLabel, String newSoundSheetName)
	{
		SoundSheet newSoundSheet = this.getNewSoundSheet(newSoundSheetName);
		this.addSoundSheetAndNotifyFragment(newSoundSheet);

		this.addSoundToSoundSheet(soundUri, soundLabel, newSoundSheet);
	}

	public void addSoundToSoundSheet(Uri soundUri, String soundLabel, SoundSheet existingSoundSheet)
	{
		MediaPlayerData mediaPlayerData;

		mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(existingSoundSheet.getFragmentTag(), soundUri, soundLabel);
		EventBus.getDefault().post(new SoundLoadedEvent(mediaPlayerData, false));
	}

	public SoundSheet getNewSoundSheet(String label)
	{
		String tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
		return new SoundSheet(null, tag, label, false);
	}

	public String getSuggestedSoundSheetName()
	{
		return this.getActivity().getResources().getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size();
	}

	/**
	 * Called by LoadSoundSheetsTask when loading of soundsheets has been finished.
	 * @param event delivered SoundSheetsLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundSheetsLoadedEvent event)
	{
		EventBus.getDefault().removeStickyEvent(event);

		this.soundSheets.addAll(event.getLoadedSoundSheets());

		BaseActivity activity = this.getBaseActivity();
		activity.handleIntent(activity.getIntent());

		SoundSheet selectedSoundSheet = findSelectedAndSelectRemaining(SoundSheetsManagerFragment.this.soundSheets);
		if (selectedSoundSheet != null)
			activity.openSoundFragment(selectedSoundSheet);
	}

	private SoundSheet findSelectedAndSelectRemaining(List<SoundSheet> soundSheets)
	{
		SoundSheet selected = null;
		if (soundSheets != null)
		{
			for (SoundSheet soundSheet : soundSheets)
			{
				if (soundSheet.getIsSelected() && selected == null)
					selected = soundSheet;
				else
					soundSheet.setIsSelected(false);
			}
		}
		return selected;
	}
}
