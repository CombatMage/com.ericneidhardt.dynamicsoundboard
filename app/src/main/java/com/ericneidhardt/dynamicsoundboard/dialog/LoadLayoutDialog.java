package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.storage.JsonPojo;
import com.ericneidhardt.dynamicsoundboard.storage.MusicService;
import com.ericneidhardt.dynamicsoundboard.storage.ServiceManagerFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundSheetManagerFragment;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public class LoadLayoutDialog extends FileExplorerDialog implements View.OnClickListener
{
	private static final String TAG = LoadLayoutDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		LoadLayoutDialog dialog = new LoadLayoutDialog();

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_load_sound_sheets, null);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_load).setOnClickListener(this);

		RecyclerView directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration(this.getActivity(), Color.TRANSPARENT));
		directories.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		directories.setItemAnimator(new DefaultItemAnimator());

		this.adapter = new DirectoryAdapter();
		directories.setAdapter(this.adapter);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
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
			case R.id.b_load:
				if (super.adapter.selectedFile != null)
					this.useFile(super.adapter.selectedFile);
				else
					Toast.makeText(this.getActivity(), R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void useFile(File file)
	{
		try
		{
			JsonPojo parsedJson  = new ObjectMapper().readValues(new JsonFactory().createParser(file), JsonPojo.class).next();

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
		SoundSheetManagerFragment soundSheetManagerFragment = this.getSoundSheetManagerFragment();// clear soundsheets before adding new values
		soundSheetManagerFragment.clearAllSoundSheets(); // this also removes all sounds

		for (SoundSheet soundSheet : soundSheets)
			soundSheetManagerFragment.addSoundSheetAndNotifyFragment(soundSheet);
	}

	private void addLoadedPlayList(List<MediaPlayerData> playList)
	{
		ServiceManagerFragment soundManagerFragment = this.getServiceManagerFragment();
		MusicService service = soundManagerFragment.getSoundService();

		service.removeFromPlaylist(soundManagerFragment.getPlayList()); // clear playlist before adding new values

		for (MediaPlayerData mediaPlayerData : playList)
			service.addSoundToPlaylist(mediaPlayerData);
		soundManagerFragment.notifyPlaylist();
	}

	private void addLoadedSounds(Map<String, List<MediaPlayerData>> sounds)
	{
		ServiceManagerFragment soundManagerFragment = this.getServiceManagerFragment();
		MusicService service = soundManagerFragment.getSoundService();

		for (String key : sounds.keySet())
		{
			List<MediaPlayerData> soundsPerFragment = sounds.get(key);
			for (MediaPlayerData mediaPlayerData : soundsPerFragment)
				service.addSound(mediaPlayerData);
		}
		soundManagerFragment.notifySoundSheetFragments();
	}

}
