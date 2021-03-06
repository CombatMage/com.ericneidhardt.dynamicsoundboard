package org.neidhardt.dynamicsoundboard.dialog.soundmanagement

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_rename_sound_file_layout.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PLAYLIST_TAG
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
class RenameSoundFileDialog : SoundSettingsBaseDialog() {

	override var player: MediaPlayerController by Delegates.notNull()
	override var fragmentTag: String by Delegates.notNull()
	override var soundSheet: SoundSheet? = null

	companion object {
		private val TAG = RenameSoundFileDialog::class.java.name

		fun show(fragmentManager: FragmentManager?, playerData: MediaPlayerData) {
			fragmentManager ?: return
			playerData.playerId ?: return
			playerData.fragmentTag ?: return

			RenameSoundFileDialog().let { dialog ->
				SoundSettingsBaseDialog.addArguments(dialog, playerData.playerId!!, playerData.fragmentTag!!)
				dialog.show(fragmentManager, TAG)
			}
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		@SuppressLint("InflateParams")
		val view = this.activity.layoutInflater.inflate(R.layout.dialog_rename_sound_file_layout, null)

		val presenter = RenameSoundFileDialogPresenter(
				playerData = this.player.mediaPlayerData,
				soundSheetManager = this.soundSheetManager,
				soundsManager = this.soundManager,
				playlistManager = this.playlistManager,
				dialog = this,
				newName = view.tv_dialog_rename_sound_file_layout_new_name,
				currentName = view.tv_dialog_rename_sound_file_layout_current_name,
				renameAllOccurrences = view.cb_dialog_rename_sound_file_layout_rename_all
		)

		return AlertDialog.Builder(context).apply {
			this.setView(view)
			this.setPositiveButton(R.string.dialog_rename_sound_file_confirm) { _, _ ->
				presenter.renameFileAndPlayer()
			}
			this.setNegativeButton(R.string.dialog_rename_sound_file_cancel) { _, _ ->
				presenter.renamePlayer()
			}
		}.create()
	}
}

private class RenameSoundFileDialogPresenter (
		private val playerData: MediaPlayerData,
		private val soundsManager: SoundManager,
		private val playlistManager: PlaylistManager,
		private val soundSheetManager: SoundSheetManager,
		private val dialog: RenameSoundFileDialog,
		private val newName : TextView,
		private val currentName : TextView,
		private val renameAllOccurrences: CheckBox
) {
	private val logger = SoundboardApplication.logger
	private val logTag = javaClass.name

	private var playersWithMatchingUri: List<MediaPlayerController>? = null

	init {
		this.playersWithMatchingUri = this.getPlayersWithMatchingUri(this.playerData.uri!!)
		if (playersWithMatchingUri!!.size > 1) {
			this.renameAllOccurrences.visibility = View.VISIBLE
			this.renameAllOccurrences.text = this.renameAllOccurrences
					.resources
					.getString(
							R.string.dialog_rename_sound_file_all_occurrences_message,
							playersWithMatchingUri!!.size
					)
		} else {
			this.renameAllOccurrences.visibility = View.GONE
		}

		val currentFile = Uri.parse(this.playerData.uri).getFileForUri()
		val currentFileName = currentFile?.name

		this.newName.text = this.appendFileTypeToNewPath(this.playerData.label, currentFileName)
		this.currentName.text = currentFileName
	}

	private fun getPlayersWithMatchingUri(uri: String): List<MediaPlayerController> {
		val playlist = this.playlistManager.playlist
		val players = playlist
				.filter { it.mediaPlayerData.uri == uri }
				.toMutableList()

		this.soundsManager.sounds.forEach { entry ->
			entry.value.forEach { player ->
				if (player.mediaPlayerData.uri == uri)
					players.add(player)
			}
		}

		return players
	}

	fun renamePlayer() {
		val newFileLabel = this.playerData.label
		val renamedPlayerId = this.playerData.playerId
		val renameAllOccurrences = this.renameAllOccurrences.isChecked

		this.playersWithMatchingUri?.forEach { player ->

			if (renameAllOccurrences || player.mediaPlayerData.playerId == renamedPlayerId) {
				player.mediaPlayerData.label = newFileLabel
			}

			if (player.mediaPlayerData.fragmentTag == PLAYLIST_TAG)
				this.playlistManager.notifyHasChanged(player)
			else
				this.soundsManager.notifyHasChanged(player)
		}

		this.dialog.dismiss()
	}

	fun renameFileAndPlayer() {
		val fileUriToRename = Uri.parse(this.playerData.uri)
		val newFileLabel = this.playerData.label
		val renamedPlayerId = this.playerData.playerId
		val renameAllOccurrences = this.renameAllOccurrences.isChecked

		val fileToRename = fileUriToRename.getFileForUri()
		if (fileToRename == null) {
			this.showErrorRenameFile()
			return
		}

		val newFilePath = fileToRename.absolutePath.replace(fileToRename.name, "") +
				this.appendFileTypeToNewPath(newFileLabel, fileToRename.name)

		if (newFilePath == fileToRename.absolutePath) {
			logger.d(logTag, "old name and new name are equal, nothing to be done")
			return
		}

		val newFile = File(newFilePath)
		val success = fileToRename.renameTo(newFile)
		if (!success) {
			this.showErrorRenameFile()
			return
		}

		val newUri = Uri.fromFile(newFile).toString()

		this.playersWithMatchingUri?.forEach { player ->
			if (!this.setUriForPlayer(player, newUri))
				this.showErrorRenameFile()

			if (renameAllOccurrences || player.mediaPlayerData.playerId == renamedPlayerId) {
				player.mediaPlayerData.label = newFileLabel
			}

			if (player.mediaPlayerData.fragmentTag == PLAYLIST_TAG)
				this.playlistManager.notifyHasChanged(player)
			else
				this.soundsManager.notifyHasChanged(player)
		}

		this.dialog.dismiss()
	}

	private fun showErrorRenameFile() {
		this.dialog.activity?.let {
			Toast.makeText(
					it, R.string.dialog_rename_sound_toast_player_not_updated, Toast.LENGTH_SHORT
			).show()
		}
	}

	private fun appendFileTypeToNewPath(newNameFilePath: String?, oldFilePath: String?): String {
		if (newNameFilePath == null || oldFilePath == null)
			throw NullPointerException(logTag + ": cannot create new file name, " +
					"either old name or new name is null")

		val segments = oldFilePath.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
		if (segments.size > 1) {
			val fileType = segments[segments.size - 1]
			return "$newNameFilePath.$fileType"
		}

		return newNameFilePath
	}

	private fun setUriForPlayer(player: MediaPlayerController, uri: String): Boolean {
		return try {
			player.setSoundUri(uri)
			true
		}
		catch (e: IOException) {
			logger.e(logTag, e.message)
			if (player.mediaPlayerData.fragmentTag == PLAYLIST_TAG)
				this.playlistManager.remove(listOf(player))
			else {
				val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(player.mediaPlayerData.fragmentTag!!)
						?: throw IllegalArgumentException("no soundSheet for given fragmentTag was found")
				this.soundsManager.remove(soundSheet, listOf(player))
			}
			false
		}
	}
}
