package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.FragmentManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsBaseDialog
import java.io.File
import java.io.IOException
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
public class RenameSoundFileDialog : SoundSettingsBaseDialog {

	private val TAG = javaClass.getName()

	private var presenter: RenameSoundFileDialogPresenter? = null

	public constructor() : super() {
	}

	public constructor(manager: FragmentManager, playerData: MediaPlayerData) : super() {
		SoundSettingsBaseDialog.addArguments(this, playerData.getPlayerId(), playerData.getFragmentTag())
		this.show(manager, TAG)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		@SuppressLint("InflateParams")
		val view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_rename_sound_file_layout, null)

		val dialog = AppCompatDialog(this.getActivity(), R.style.DialogTheme)
		dialog.setContentView(view)
		dialog.setTitle(R.string.dialog_rename_sound_file_title)

		val presenter = RenameSoundFileDialogPresenter(
				playerData = this.player.getMediaPlayerData(),
				soundsDataAccess = DynamicSoundboardApplication.getApplicationComponent().provideSoundsDataAccess(),
				soundsDataStorage = DynamicSoundboardApplication.getApplicationComponent().provideSoundsDataStorage(),
				dialog = this,
				currentName = view.findViewById(R.id.tv_current_name),
				newName = view.findViewById(R.id.tv_new_name),
				renameAllOccurrences = view.findViewById(R.id.cb_rename_all_occurrences) as CheckBox
		)
		view.findViewById(R.id.b_ok).setOnClickListener({ view -> presenter.rename() })
		view.findViewById(R.id.b_cancel).setOnClickListener({ view -> this.dismiss() })

		this.presenter = presenter
		return dialog
	}
}
private class RenameSoundFileDialogPresenter
(
		private val playerData: MediaPlayerData,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage,
		private val dialog: RenameSoundFileDialog,
		private val currentName : TextView,
		private val newName : TextView,
		private val renameAllOccurrences: CheckBox
)
{
	private val TAG = javaClass.getName()

	private var playersWithMatchingUri: List<EnhancedMediaPlayer>? = null

	init
	{
		this.playersWithMatchingUri = this.getPlayersWithMatchingUri(this.playerData.getUri())
		if (playersWithMatchingUri!!.size() > 1)
		{
			this.renameAllOccurrences.setVisibility(View.VISIBLE)
			this.renameAllOccurrences.setText(this.renameAllOccurrences.getText().toString()
					.replace("{%s0}", Integer.toString(playersWithMatchingUri!!.size())))
		}
		else
			this.renameAllOccurrences.setVisibility(View.GONE)

		this.newName.setText(this.playerData.getLabel())
	}

	private fun getPlayersWithMatchingUri(uri: String): List<EnhancedMediaPlayer>
	{
		val players = ArrayList<EnhancedMediaPlayer>()

		val playlist = this.soundsDataAccess.getPlaylist()
		for (player in playlist)
		{
			if (player.getMediaPlayerData().getUri() == uri)
				players.add(player)
		}

		val fragments = this.soundsDataAccess.getSounds().keySet()
		for (fragment in fragments)
		{
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(fragment)
			for (player in soundsInFragment)
			{
				if (player.getMediaPlayerData().getUri() == uri)
					players.add(player)
			}
		}

		return players
	}

	internal fun rename()
	{
		val uri = Uri.parse(this.playerData.getUri())
		val label = this.playerData.getLabel()
		val renameAllOccurrences = this.renameAllOccurrences.isChecked()

		this.deliverResult(uri, label, renameAllOccurrences)

		this.dialog.dismiss()
	}

	private fun deliverResult(fileUriToRename: Uri, newFileLabel: String, renameAllOccurrences: Boolean) {
		val fileToRename = FileUtils.getFileForUri(fileUriToRename)
		if (fileToRename == null)
		{
			this.showErrorRenameFile()
			return
		}

		val newFilePath = fileToRename.getAbsolutePath().replace(fileToRename.getName(), "") + this.appendFileTypeToNewPath(newFileLabel, fileToRename.getName())
		if (newFilePath == fileToRename.getAbsolutePath())
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
				player.getMediaPlayerData().setLabel(newFileLabel)
				player.getMediaPlayerData().updateItemInDatabaseAsync()
			}

			if (player.getMediaPlayerData().getFragmentTag() == Playlist.TAG)
				EventBus.getDefault().post(PlaylistChangedEvent())
			else
				EventBus.getDefault().post(SoundChangedEvent(player))
		}
	}

	private fun showErrorRenameFile()
	{
		if (this.dialog.getActivity() != null)
			Toast.makeText(this.dialog.getActivity(), R.string.dialog_rename_sound_toast_player_not_updated, Toast.LENGTH_SHORT).show()
	}

	private fun appendFileTypeToNewPath(newNameFilePath: String?, oldFilePath: String?): String
	{
		if (newNameFilePath == null || oldFilePath == null)
			throw NullPointerException(TAG + ": cannot create new file name, either old name or new name is null")

		val segments = oldFilePath.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		if (segments.size() > 1) {
			val fileType = segments[segments.size() - 1]
			return newNameFilePath + "." + fileType
		}

		return newNameFilePath
	}

	private fun setUriForPlayer(player: EnhancedMediaPlayer, uri: String): Boolean
	{
		try
		{
			player.setSoundUri(uri)
			return true
		}
		catch (e: IOException)
		{
			Logger.e(TAG, e.getMessage())
			if (player.getMediaPlayerData().getFragmentTag() == Playlist.TAG)
				this.soundsDataStorage.removeSoundsFromPlaylist(listOf(player))
			else
				this.soundsDataStorage.removeSounds(listOf(player))
			return false
		}
	}
}
