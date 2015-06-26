package org.neidhardt.dynamicsoundboard.fileexplorer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.JsonPojo;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsFromFileLoadedEvent;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public class LoadLayoutDialog extends FileExplorerDialog implements View.OnClickListener
{
	private static final String TAG = LoadLayoutDialog.class.getName();

	private View confirm;
	private RecyclerView directories;

	public static void showInstance(FragmentManager manager)
	{
		LoadLayoutDialog dialog = new LoadLayoutDialog();
		dialog.show(manager, TAG);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_load_sound_sheets, null);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		this.confirm = view.findViewById(R.id.b_ok);
		this.confirm.setOnClickListener(this);
		this.confirm.setEnabled(false);

		this.adapter = new DirectoryAdapter();

		this.directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		this.directories.addItemDecoration(new DividerItemDecoration());
		this.directories.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		this.directories.setItemAnimator(new DefaultItemAnimator());
		this.directories.setAdapter(this.adapter);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogThemeNoTitle);
		dialog.setContentView(view);

		return dialog;
	}

	@Override
	protected void onFileSelected()
	{
		this.confirm.setEnabled(true);
		int position = this.adapter.fileList.indexOf(this.adapter.selectedFile);
		this.directories.scrollToPosition(position);
	}

	@Override
	protected boolean canSelectDirectory()
	{
		return false;
	}

	@Override
	protected boolean canSelectFile()
	{
		return true;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_ok:
				if (super.adapter.selectedFile != null)
					this.loadFromFileAndDismiss(this.adapter.selectedFile);
				else
					Toast.makeText(this.getActivity(), R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void loadFromFileAndDismiss(File file)
	{
		try
		{
			JsonPojo parsedJson = JsonPojo.readFromFile(file);

			List<SoundSheet> soundSheets = parsedJson.getSoundSheets();
			List<MediaPlayerData> playList = parsedJson.getPlayList();
			Map<String, List<MediaPlayerData>> sounds = parsedJson.getSounds();

			this.addLoadedSoundSheets(soundSheets);

			this.addLoadedSounds(sounds);
			this.addLoadedPlayList(playList);

			this.dismiss();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void addLoadedSoundSheets(List<SoundSheet> soundSheets)
	{
		List<SoundSheet> oldCurrentSoundSheet = this.soundSheetsDataAccess.getSoundSheets();

		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>();
		for (SoundSheet soundSheet : oldCurrentSoundSheet)
			playersToRemove.addAll(this.soundsDataAccess.getSoundsInFragment(soundSheet.getFragmentTag()));

		this.soundsDataStorage.removeSounds(playersToRemove);

		EventBus.getDefault().post(new SoundSheetsFromFileLoadedEvent(soundSheets, oldCurrentSoundSheet));
	}

	private void addLoadedPlayList(List<MediaPlayerData> playList)
	{
		this.soundsDataStorage.removeSoundsFromPlaylist(this.soundsDataAccess.getPlaylist()); // clear playlist before adding new values

		for (MediaPlayerData mediaPlayerData : playList)
			this.soundsDataStorage.createPlaylistSoundAndAddToManager(mediaPlayerData);
	}

	private void addLoadedSounds(Map<String, List<MediaPlayerData>> sounds)
	{
		for (String key : sounds.keySet())
		{
			List<MediaPlayerData> soundsPerFragment = sounds.get(key);
			for (MediaPlayerData mediaPlayerData : soundsPerFragment)
				this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData);
		}
	}

}
