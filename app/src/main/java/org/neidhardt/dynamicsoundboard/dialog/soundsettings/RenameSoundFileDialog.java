package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
		MediaPlayerData playerData = this.player.getMediaPlayerData();

		File fileToRename = FileUtils.getFileForUri(this.getActivity(), Uri.parse(playerData.getUri()));
		if (fileToRename == null)
		{
			Toast.makeText(this.getActivity(), R.string.dialog_rename_sound_toast_file_not_found, Toast.LENGTH_SHORT).show();
			return;
		}

		String newFilePath = fileToRename.getAbsolutePath().replace(fileToRename.getName(), "") + this.appendFileTypeToNewPath(playerData.getLabel(), fileToRename.getName());
		File newFile = new File(newFilePath);
		boolean success = fileToRename.renameTo(newFile);
		if (!success)
		{
			Toast.makeText(this.getActivity(), R.string.dialog_rename_sound_toast_file_not_found, Toast.LENGTH_SHORT).show();
			return;
		}

		List<EnhancedMediaPlayer> playersWithMatchingUri = this.getPlayersWithMatchingUri(playerData.getUri());
		Uri uri = Uri.fromFile(fileToRename);
		String newUri = uri.toString();
		for (EnhancedMediaPlayer player : playersWithMatchingUri)
			this.setUriForPlayer(player, newUri);
	}

	String appendFileTypeToNewPath(String newNameFilePath, String oldFilePath)
	{
		if (newNameFilePath == null || oldFilePath == null)
			throw new NullPointerException(TAG + ": cannot create new file name, either old name or new name is null");

		String[] segments = oldFilePath.split("\\.");
		if (segments.length > 1) {
			String fileType = segments[segments.length - 1];
			return newNameFilePath + "." + fileType;
		}

		return newNameFilePath;
	}

	void setUriForPlayer(EnhancedMediaPlayer player, String uri)
	{
		MusicService service = this.getServiceManagerFragment().getSoundService();
		try
		{
			player.setSoundUri(uri);
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			if (player.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG))
			{
				service.removeFromPlaylist(Collections.singletonList(player));
				this.getServiceManagerFragment().notifyPlaylist();
			}
			else
			{
				service.removeSounds(Collections.singletonList(player));
				this.getServiceManagerFragment().notifyFragment(player.getMediaPlayerData().getFragmentTag());
			}
		}
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
