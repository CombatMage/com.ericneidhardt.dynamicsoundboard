package org.neidhardt.dynamicsoundboard.fileexplorer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.JsonPojo;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.AddNewSoundEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsFromFileLoadedEvent;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

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
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_load_sound_sheets, null);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_load).setOnClickListener(this);

		RecyclerView directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration());
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

			ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
			addLoadedSounds(sounds);
			addLoadedPlayList(playList, serviceManagerFragment);

			this.dismiss();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void addLoadedSoundSheets(List<SoundSheet> soundSheets)
	{
		List<SoundSheet> currentSoundSheet = this.soundSheetsDataAccess.getSoundSheets();
		EventBus.getDefault().post(new SoundSheetsFromFileLoadedEvent(soundSheets, currentSoundSheet));
	}

	private static void addLoadedPlayList(List<MediaPlayerData> playList, ServiceManagerFragment soundManagerFragment)
	{
		soundManagerFragment.removeSoundsFromPlaylist(soundManagerFragment.getPlayList()); // clear playlist before adding new values

		EventBus bus = EventBus.getDefault();
		for (MediaPlayerData mediaPlayerData : playList)
			bus.post(new PlaylistLoadedEvent(mediaPlayerData, false));
	}

	private static void addLoadedSounds(Map<String, List<MediaPlayerData>> sounds)
	{
		EventBus bus = EventBus.getDefault();
		for (String key : sounds.keySet())
		{
			List<MediaPlayerData> soundsPerFragment = sounds.get(key);
			for (MediaPlayerData mediaPlayerData : soundsPerFragment)
				bus.post(new AddNewSoundEvent(mediaPlayerData, false));
		}
	}

}
