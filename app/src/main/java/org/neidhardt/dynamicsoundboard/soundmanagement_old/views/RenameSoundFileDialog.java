package org.neidhardt.dynamicsoundboard.soundmanagement_old.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.model.SoundDataModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * File created by eric.neidhardt on 12.04.2015.
 */
public class RenameSoundFileDialog extends SoundSettingsBaseDialog implements View.OnClickListener
{
	public static final String TAG = RenameSoundFileDialog.class.getName();

	private CheckBox renameAllOccurrences;
	private MediaPlayerData playerData;
	private List<EnhancedMediaPlayer> playersWithMatchingUri;

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
		this.renameAllOccurrences = (CheckBox) view.findViewById(R.id.cb_rename_all_occurrences);

		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		if (this.player != null)
			this.setMediaPlayerData(this.player.getMediaPlayerData());

		return builder.create();
	}

	void setMediaPlayerData(MediaPlayerData playerData)
	{
		this.playerData = playerData;

		this.playersWithMatchingUri = this.getPlayersWithMatchingUri(this.playerData.getUri());
		if (playersWithMatchingUri.size() > 1)
		{
			this.renameAllOccurrences.setVisibility(View.VISIBLE);
			this.renameAllOccurrences.setText(this.renameAllOccurrences.getText().toString().replace("{%s0}", Integer.toString(playersWithMatchingUri.size())));
		}
		else
			this.renameAllOccurrences.setVisibility(View.GONE);
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
				this.deliverResult(Uri.parse(this.playerData.getUri()), this.playerData.getLabel(), this.renameAllOccurrences.isChecked());
				this.dismiss();
				break;
		}
	}

	void deliverResult(Uri fileUriToRename, String newFileLabel, boolean renameAllOccurrences) {
		File fileToRename = FileUtils.getFileForUri(this.getActivity(), fileUriToRename);
		if (fileToRename == null) {
			this.showErrorRenameFile();
			return;
		}

		String newFilePath = fileToRename.getAbsolutePath().replace(fileToRename.getName(), "") + this.appendFileTypeToNewPath(newFileLabel, fileToRename.getName());
		if (newFilePath.equals(fileToRename.getAbsolutePath())) {
			Logger.d(TAG, "old name and new name are equal, nothing to be done");
			return;
		}

		File newFile = new File(newFilePath);
		boolean success = fileToRename.renameTo(newFile);
		if (!success) {
			this.showErrorRenameFile();
			return;
		}

		String newUri = Uri.fromFile(newFile).toString();
		for (EnhancedMediaPlayer player : this.playersWithMatchingUri) {
			if (!this.setUriForPlayer(player, newUri))
				this.showErrorRenameFile();

			if (renameAllOccurrences) {
				player.getMediaPlayerData().setLabel(newFileLabel);
				player.getMediaPlayerData().setItemWasAltered();
			}

			if (player.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG))
				EventBus.getDefault().post(new PlaylistChangedEvent());
			else
				this.notifyFragment(player.getMediaPlayerData().getFragmentTag());
		}
	}

	private void showErrorRenameFile()
	{
		if (this.getActivity() != null)
			Toast.makeText(this.getActivity(), R.string.dialog_rename_sound_toast_player_not_updated, Toast.LENGTH_SHORT).show();
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

	boolean setUriForPlayer(EnhancedMediaPlayer player, String uri)
	{
		SoundDataModel model = this.getServiceManagerFragment();
		try
		{
			player.setSoundUri(uri);
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			if (player.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG))
				model.removeSoundsFromPlaylist(Collections.singletonList(player));
			else
				model.removeSounds(Collections.singletonList(player));
			return false;
		}
	}

	List<EnhancedMediaPlayer> getPlayersWithMatchingUri(String uri)
	{
		SoundDataModel model = this.getServiceManagerFragment();
		List<EnhancedMediaPlayer> players = new ArrayList<>();

		for (EnhancedMediaPlayer player : model.getPlayList())
		{
			if (player.getMediaPlayerData().getUri().equals(uri))
				players.add(player);
		}

		Set<String> fragments = model.getSounds().keySet();
		for (String fragment : fragments)
		{
			List<EnhancedMediaPlayer> soundsInFragment = model.getSoundsInFragment(fragment);
			for (EnhancedMediaPlayer player : soundsInFragment)
			{
				if (player.getMediaPlayerData().getUri().equals(uri))
					players.add(player);
			}
		}

		return players;
	}
}
