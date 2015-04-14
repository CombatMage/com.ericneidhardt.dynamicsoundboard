package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dialog.BaseDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Created by eric.neidhardt on 12.04.2015.
 */
public class RenameSoundFileDialog extends SoundSettingsBaseDialog implements View.OnClickListener
{
	public static final String TAG = RenameSoundFileDialog.class.getName();

	public static void showInstance(FragmentManager manager, MediaPlayerData playerData)
	{
		RenameSoundFileDialog dialog = new RenameSoundFileDialog();
		addArguments(dialog, playerData.getPlayerId(), playerData.getFragmentTag());
		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams")
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_rename_sound_file_layout, null);

		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
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
				this.deliverResult();
				this.dismiss();
				break;
		}
	}

	private void deliverResult()
	{
		MusicService service = this.getServiceManagerFragment().getSoundService();
		MediaPlayerData playerData = this.player.getMediaPlayerData();

		File correspondingSoundFile = FileUtils.getFileForUri(this.getActivity(), Uri.parse(playerData.getUri()));


		// TODO rename File and update playerData
	}

	List<EnhancedMediaPlayer> getPlayersWithMatchingUri(String uri)
	{
		MusicService service = this.getServiceManagerFragment().getSoundService();
		List<EnhancedMediaPlayer> players = new ArrayList<>();

		for (EnhancedMediaPlayer player : service.getPlaylist())
		{
			if (player.getMediaPlayerData().getUri().equals(uri))
				players.add(player);
		}

		Set<String> fragments = service.getSounds().keySet();
		for (String fragment : fragments)
		{
			List<EnhancedMediaPlayer> soundsInFragment = service.getSounds().get(fragment);
			for (EnhancedMediaPlayer player : soundsInFragment)
			{
				if (player.getMediaPlayerData().getUri().equals(uri))
					players.add(player);
			}
		}

		return players;
	}
}
