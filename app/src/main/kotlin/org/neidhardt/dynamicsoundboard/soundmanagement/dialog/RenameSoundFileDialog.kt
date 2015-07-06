package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.FragmentManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsBaseDialog
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 06.07.2015.
 */

public class RenameSoundFileDialog(manager: FragmentManager, playerData: MediaPlayerData)
{
	private val TAG: String = javaClass.getName()

	init
	{
		val dialog = RenameSoundFileDialogView()
		SoundSettingsBaseDialog.addArguments(dialog, playerData.getPlayerId(), playerData.getFragmentTag())
		dialog.show(manager, TAG)
	}
}

private class RenameSoundFileDialogView() : SoundSettingsBaseDialog(), View.OnClickListener {

	private val TAG = javaClass.getName()

	private var renameAllOccurrences: CheckBox? = null
	private var playerData: MediaPlayerData? = null
	private var playersWithMatchingUri: List<EnhancedMediaPlayer>? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		@SuppressLint("InflateParams")
		val view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_rename_sound_file_layout, null)
		this.renameAllOccurrences = view.findViewById(R.id.cb_rename_all_occurrences) as CheckBox

		view.findViewById(R.id.b_ok).setOnClickListener(this)
		view.findViewById(R.id.b_cancel).setOnClickListener(this)

		val dialog = AppCompatDialog(this.getActivity(), R.style.DialogTheme)
		dialog.setContentView(view)
		dialog.setTitle(R.string.dialog_rename_sound_file_title)

		if (this.player != null)
			this.setMediaPlayerData(this.player.getMediaPlayerData())

		return dialog
	}

	fun setMediaPlayerData(playerData: MediaPlayerData) {
		this.playerData = playerData

		this.playersWithMatchingUri = this.getPlayersWithMatchingUri(this.playerData!!.getUri())
		if (playersWithMatchingUri!!.size() > 1) {
			this.renameAllOccurrences!!.setVisibility(View.VISIBLE)
			this.renameAllOccurrences!!.setText(this.renameAllOccurrences!!.getText().toString().replace("{%s0}", Integer.toString(playersWithMatchingUri!!.size())))
		} else
			this.renameAllOccurrences!!.setVisibility(View.GONE)
	}

	override fun onClick(v: View) {
		when (v.getId()) {
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok -> {
				this.deliverResult(Uri.parse(this.playerData!!.getUri()), this.playerData!!.getLabel(), this.renameAllOccurrences!!.isChecked())
				this.dismiss()
			}
		}
	}

	fun deliverResult(fileUriToRename: Uri, newFileLabel: String, renameAllOccurrences: Boolean) {
		val fileToRename = FileUtils.getFileForUri(this.getActivity(), fileUriToRename)
		if (fileToRename == null) {
			this.showErrorRenameFile()
			return
		}

		val newFilePath = fileToRename.getAbsolutePath().replace(fileToRename.getName(), "") + this.appendFileTypeToNewPath(newFileLabel, fileToRename.getName())
		if (newFilePath == fileToRename.getAbsolutePath()) {
			Logger.d(TAG, "old name and new name are equal, nothing to be done")
			return
		}

		val newFile = File(newFilePath)
		val success = fileToRename.renameTo(newFile)
		if (!success) {
			this.showErrorRenameFile()
			return
		}

		val newUri = Uri.fromFile(newFile).toString()
		for (player in this.playersWithMatchingUri!!) {
			if (!this.setUriForPlayer(player, newUri))
				this.showErrorRenameFile()

			if (renameAllOccurrences) {
				player.getMediaPlayerData().setLabel(newFileLabel)
				player.getMediaPlayerData().updateItemInDatabaseAsync()
			}

			if (player.getMediaPlayerData().getFragmentTag() == Playlist.TAG)
				EventBus.getDefault().post(PlaylistChangedEvent())
			else
				EventBus.getDefault().post(SoundChangedEvent(player))
		}
	}

	private fun showErrorRenameFile() {
		if (this.getActivity() != null)
			Toast.makeText(this.getActivity(), R.string.dialog_rename_sound_toast_player_not_updated, Toast.LENGTH_SHORT).show()
	}

	fun appendFileTypeToNewPath(newNameFilePath: String?, oldFilePath: String?): String {
		if (newNameFilePath == null || oldFilePath == null)
			throw NullPointerException(TAG + ": cannot create new file name, either old name or new name is null")

		val segments = oldFilePath.split("\\.")
		if (segments.size() > 1) {
			val fileType = segments[segments.size() - 1]
			return newNameFilePath + "." + fileType
		}

		return newNameFilePath
	}

	fun setUriForPlayer(player: EnhancedMediaPlayer, uri: String): Boolean {
		try {
			player.setSoundUri(uri)
			return true
		} catch (e: IOException) {
			Logger.e(TAG, e.getMessage())
			if (player.getMediaPlayerData().getFragmentTag() == Playlist.TAG)
				this.soundsDataStorage.removeSoundsFromPlaylist(listOf(player))
			else
				this.soundsDataStorage.removeSounds(listOf(player))
			return false
		}

	}

	fun getPlayersWithMatchingUri(uri: String): List<EnhancedMediaPlayer> {
		val players = ArrayList<EnhancedMediaPlayer>()

		val playlist = this.soundsDataAccess.getPlaylist()
		for (player in playlist) {
			if (player.getMediaPlayerData().getUri() == uri)
				players.add(player)
		}

		val fragments = this.soundsDataAccess.getSounds().keySet()
		for (fragment in fragments) {
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(fragment)
			for (player in soundsInFragment) {
				if (player.getMediaPlayerData().getUri() == uri)
					players.add(player)
			}
		}

		return players
	}
}
