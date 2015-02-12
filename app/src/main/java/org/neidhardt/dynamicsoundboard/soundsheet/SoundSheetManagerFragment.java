package org.neidhardt.dynamicsoundboard.soundsheet;


import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import org.neidhardt.dynamicsoundboard.*;
import org.neidhardt.dynamicsoundboard.customview.ActionbarEditText;
import org.neidhardt.dynamicsoundboard.customview.CustomEditText;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.AddNewSoundFromIntent;
import org.neidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetManagerFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			CustomEditText.OnTextEditedListener
{
	public static final String TAG = SoundSheetManagerFragment.class.getName();

	private static final String DB_SOUNDS = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets";

	private List<SoundSheet> soundSheets;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.soundSheets = new ArrayList<>();
		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUNDS);

		SafeAsyncTask task = new LoadSoundSheetsTask();
		task.execute();
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
	public void onPause()
	{
		super.onPause();

		SafeAsyncTask task = new UpdateSoundSheetsTask(this.soundSheets);
		task.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_clear_sound_sheets:
				this.clearAllSoundSheets();
				return true;
			default:
				return false;
		}
	}

	public void clearAllSoundSheets()
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

		fragment.notifyPlaylist();

		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawer();
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);
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
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawer();
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false);
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
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);

		if (notifySoundSheets)
			navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);

		SafeAsyncTask task = new RemoveSoundSheetTask(soundSheet);
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

	public List<SoundSheet> getAll()
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
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);

		SafeAsyncTask task = new StoreSoundSheetTask(soundSheet);
		task.execute();
	}

	public void addSoundFromIntent(Uri soundUri, String soundLabel, String newSoundSheetName, SoundSheet existingSoundSheet)
	{
		ServiceManagerFragment soundManagerFragment = this.getServiceManagerFragment();
		MusicService service = soundManagerFragment.getSoundService();
		MediaPlayerData mediaPlayerData;
		if (newSoundSheetName != null)
		{
			SoundSheet newSoundSheet = this.getNewSoundSheet(newSoundSheetName);
			this.addSoundSheetAndNotifyFragment(newSoundSheet);
			mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(newSoundSheet.getFragmentTag(), soundUri, soundLabel);

			service.addNewSoundToServiceAndDatabase(mediaPlayerData);
			soundManagerFragment.notifyFragment(mediaPlayerData.getFragmentTag());
		}
		else if (existingSoundSheet != null)
			mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(existingSoundSheet.getFragmentTag(), soundUri, soundLabel);
		else
			throw new NullPointerException(TAG + ".addSoundFromIntent: cannot add new sound, mediaPlayerData is null");
		service.addNewSoundToServiceAndDatabase(mediaPlayerData);
		soundManagerFragment.notifyFragment(mediaPlayerData.getFragmentTag());
	}

	public SoundSheet getNewSoundSheet(String label)
	{
		String tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
		return new SoundSheet(null, tag, label, false);
	}

	private void handleIntent(Intent intent)
	{
		if (intent == null)
			return;

		String action = intent.getAction();
		if (action == null)
			return;

		if (intent.getAction().equals(Intent.ACTION_VIEW)
				&& intent.getData() != null)
		{
			if (this.soundSheets.size() == 0)
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(), this.getSuggestedSoundSheetName(), null);
			else
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(), this.getSuggestedSoundSheetName(), this.soundSheets);
		}
	}

	public String getSuggestedSoundSheetName()
	{
		return this.getActivity().getResources().getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size();
	}

	private class RemoveSoundSheetTask extends SafeAsyncTask<Void>
	{
		private SoundSheet soundSheet;

		public RemoveSoundSheetTask(SoundSheet soundSheet)
		{
			this.soundSheet = soundSheet;
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getSoundSheetDao().delete(this.soundSheet);
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

	private class LoadSoundSheetsTask extends SafeAsyncTask<List<SoundSheet>>
	{
		@Override
		public List<SoundSheet> call() throws Exception
		{
			return daoSession.getSoundSheetDao().queryBuilder().list();
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
		@Override
		protected void onSuccess(List<SoundSheet> loadedSoundSheets) throws Exception
		{
			super.onSuccess(loadedSoundSheets);

			if (loadedSoundSheets.size() > 0)
				SoundSheetManagerFragment.this.soundSheets.addAll(loadedSoundSheets);

			handleIntent(getActivity().getIntent());
			NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager()
					.findFragmentByTag(NavigationDrawerFragment.TAG);
			navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);

			SoundSheet selectedSoundSheet = findSelectedAndSelectRemaining(SoundSheetManagerFragment.this.soundSheets);
			if (selectedSoundSheet != null)
				getBaseActivity().openSoundFragment(selectedSoundSheet);
		}
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

	private class StoreSoundSheetTask extends SafeAsyncTask<Void>
	{
		private final String TAG = StoreSoundSheetTask.class.getName();
		private SoundSheet soundSheet;

		public StoreSoundSheetTask(SoundSheet soundSheet)
		{
			this.soundSheet = soundSheet;
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getSoundSheetDao().insertInTx(this.soundSheet);
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

	private class UpdateSoundSheetsTask extends SafeAsyncTask<Void>
	{
		private final List<SoundSheet> soundSheets;

		private UpdateSoundSheetsTask(List<SoundSheet> soundSheets)
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
