package com.ericneidhardt.dynamicsoundboard.storage;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.*;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromIntent;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

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

	private static final String DB_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.SoundSheetManagerFragment.db_sound_sheets";

	private List<SoundSheet> soundSheets;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.soundSheets = new ArrayList<SoundSheet>();
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
		SoundSheet currentActiveSoundSheet = this.getSelectedItem();
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
				BaseActivity activity = (BaseActivity) this.getActivity();
				activity.removeSoundFragment(this.soundSheets);
				activity.setSoundSheetActionsEnable(false);

				SoundManagerFragment fragment = this.getSoundManagerFragment();
				for (SoundSheet soundSheet : this.soundSheets)
				{
					List<EnhancedMediaPlayer> soundsInSoundSheet = fragment.getSounds().get(soundSheet.getFragmentTag());
					fragment.removeSounds(soundsInSoundSheet);
				}
				this.soundSheets.clear();

				fragment.notifyPlaylist();

				NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawer();
				navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);

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
		SoundSheet currentActiveSoundSheet = this.getSelectedItem();
		if (currentActiveSoundSheet == null)
			throw new NullPointerException("sound sheet label was edited, but no sound sheet is selected");

		currentActiveSoundSheet.setLabel(text);
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);
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

	public void remove(String fragmentTag, boolean notifySoundSheets)
	{
		SoundSheet soundSheetToRemove = null;
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getFragmentTag().equals(fragmentTag))
				soundSheetToRemove = soundSheet;
		}
		if (soundSheetToRemove != null)
			this.remove(soundSheetToRemove, notifySoundSheets);
	}

	public List<SoundSheet> getAll()
	{
		return this.soundSheets;
	}

	public SoundSheet getSelectedItem()
	{
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getIsSelected())
				return soundSheet;
		}
		return null;
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
		SoundManagerFragment soundManagerFragment = this.getSoundManagerFragment();
		if (soundManagerFragment == null)
			throw new NullPointerException("cannot addSoundSheetAndNotifyFragment sound, SoundManagerFragment is null");

		MediaPlayerData mediaPlayerData;
		if (newSoundSheetName != null)
		{
			SoundSheet newSoundSheet = this.getNewSoundSheet(newSoundSheetName);
			this.addSoundSheetAndNotifyFragment(newSoundSheet);
			mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(newSoundSheet.getFragmentTag(), soundUri, soundLabel);

			soundManagerFragment.addSound(mediaPlayerData);
			soundManagerFragment.notifyFragment(mediaPlayerData.getFragmentTag());
		}
		else if (existingSoundSheet != null)
			mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(existingSoundSheet.getFragmentTag(), soundUri, soundLabel);
		else
			throw new NullPointerException(TAG + ".addSoundFromIntent: cannot add new sound, mediaPlayerData is null");
		soundManagerFragment.addSound(mediaPlayerData);
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
		protected void onSuccess(List<SoundSheet> loadedSoundSheets) throws Exception
		{
			super.onSuccess(loadedSoundSheets);

			if (loadedSoundSheets.size() > 0)
				SoundSheetManagerFragment.this.soundSheets.addAll(loadedSoundSheets);

			handleIntent(getActivity().getIntent());
			NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager()
					.findFragmentByTag(NavigationDrawerFragment.TAG);
			navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(true);

			BaseActivity activity = (BaseActivity)getActivity();
			SoundSheet selectedSoundSheet = getSelectedItem();
			if (selectedSoundSheet != null)
				activity.openSoundFragment(selectedSoundSheet);

		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private class StoreSoundSheetTask extends SafeAsyncTask<Void>
	{
		private final String TAG = StoreSoundSheetTask.class.getSimpleName();
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
