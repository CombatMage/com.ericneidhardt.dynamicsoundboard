package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

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
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsBaseDialog
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
class RenameSoundFileDialog : SoundSettingsBaseDialog
{
	override var player: MediaPlayerController? = null
	override var fragmentTag: String? = null

	private val TAG = javaClass.name

	constructor() : super()

	constructor(manager: FragmentManager, playerData: MediaPlayerData) : super()
	{
		SoundSettingsBaseDialog.addArguments(this, playerData.playerId, playerData.fragmentTag)
		this.show(manager, TAG)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams")
		val view = this.activity.layoutInflater.inflate(R.layout.dialog_rename_sound_file_layout, null)

		val presenter = RenameSoundFileDialogPresenter(
				playerData = this.player!!.mediaPlayerData,
				soundsDataAccess = this.soundsDataAccess,
				soundsDataStorage = this.soundsDataStorage,
				dialog = this,
				newName = view.findViewById(R.id.tv_new_name) as TextView,
				renameAllOccurrences = view.findViewById(R.id.cb_rename_all_occurrences) as CheckBox
		)

		return AlertDialog.Builder(context).apply {
			this.setView(view)
			this.setPositiveButton(R.string.dialog_rename, { dialogInterface, i ->
				presenter.rename()
			})
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss() })
		}.create()
	}
}

private class RenameSoundFileDialogPresenter
(
		private val playerData: MediaPlayerData,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage,
		private val dialog: RenameSoundFileDialog,
		private val newName : TextView,
		private val renameAllOccurrences: CheckBox
)
{
	private val TAG = javaClass.name

	private var playersWithMatchingUri: List<MediaPlayerController>? = null

	init
	{
		this.playersWithMatchingUri = this.getPlayersWithMatchingUri(this.playerData.uri)
		if (playersWithMatchingUri!!.size > 1)
		{
			this.renameAllOccurrences.visibility = View.VISIBLE
			this.renameAllOccurrences.text = this.renameAllOccurrences.text.toString()
					.replace("{%s0}", Integer.toString(playersWithMatchingUri!!.size))
		}
		else
			this.renameAllOccurrences.visibility = View.GONE

		val currentFile = Uri.parse(this.playerData.uri).getFileForUri()
		val currentFileName = currentFile?.name

		this.newName.text = this.appendFileTypeToNewPath(this.playerData.label, currentFileName)
	}

	private fun getPlayersWithMatchingUri(uri: String): List<MediaPlayerController>
	{
		val players = ArrayList<MediaPlayerController>()

		val playlist = this.soundsDataAccess.playlist
		for (player in playlist)
		{
			if (player.mediaPlayerData.uri == uri)
				players.add(player)
		}

		val fragments = this.soundsDataAccess.sounds.keys
		for (fragment in fragments)
		{
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(fragment)
			for (player in soundsInFragment)
			{
				if (player.mediaPlayerData.uri == uri)
					players.add(player)
			}
		}

		return players
	}

	internal fun rename()
	{
		val uri = Uri.parse(this.playerData.uri)
		val label = this.playerData.label
		val renameAllOccurrences = this.renameAllOccurrences.isChecked

		this.deliverResult(uri, label, renameAllOccurrences)

		this.dialog.dismiss()
	}

	private fun deliverResult(fileUriToRename: Uri, newFileLabel: String, renameAllOccurrences: Boolean)
	{
		val fileToRename = fileUriToRename.getFileForUri()
		if (fileToRename == null)
		{
			this.showErrorRenameFile()
			return
		}

		val newFilePath = fileToRename.absolutePath.replace(fileToRename.name, "") + this.appendFileTypeToNewPath(newFileLabel, fileToRename.name)
		if (newFilePath == fileToRename.absolutePath)
		{
			Logger.d(TAG, "old name and new name are equal, nothing to be done")
			return
		}

		val newFile = File(newFilePath)
		val success = fileToRename.renameTo(newFile)
		if (!success)
		{
			this.showErrorRenameFile()
			return
		}

		val newUri = Uri.fromFile(newFile).toString()
		for (player in this.playersWithMatchingUri!!)
		{
			if (!this.setUriForPlayer(player, newUri))
				this.showErrorRenameFile()

			if (renameAllOccurrences)
			{
				player.mediaPlayerData.label = newFileLabel
				player.mediaPlayerData.updateItemInDatabaseAsync()
			}

			if (player.mediaPlayerData.fragmentTag == PlaylistTAG)
				EventBus.getDefault().post(PlaylistChangedEvent())
			else
				EventBus.getDefault().post(SoundChangedEvent(player))
		}
	}

	private fun showErrorRenameFile()
	{
		if (this.dialog.activity != null)
			Toast.makeText(this.dialog.activity, R.string.dialog_rename_sound_toast_player_not_updated, Toast.LENGTH_SHORT).show()
	}

	private fun appendFileTypeToNewPath(newNameFilePath: String?, oldFilePath: String?): String
	{
		if (newNameFilePath == null || oldFilePath == null)
			throw NullPointerException(TAG + ": cannot create new file name, either old name or new name is null")

		val segments = oldFilePath.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		if (segments.size > 1) {
			val fileType = segments[segments.size - 1]
			return newNameFilePath + "." + fileType
		}

		return newNameFilePath
	}

	private fun setUriForPlayer(player: MediaPlayerController, uri: String): Boolean
	{
		try
		{
			player.setSoundUri(uri)
			return true
		}
		catch (e: IOException)
		{
			Logger.e(TAG, e.message)
			if (player.mediaPlayerData.fragmentTag == PlaylistTAG)
				this.soundsDataStorage.removeSoundsFromPlaylist(listOf(player))
			else
				this.soundsDataStorage.removeSounds(listOf(player))
			return false
		}
	}
}
