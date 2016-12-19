package org.neidhardt.dynamicsoundboard.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.readFromFile
import org.neidhardt.dynamicsoundboard.reportError
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
class LoadLayoutDialog : FileExplorerDialog(), LayoutStorageDialog
{
	private var directories: RecyclerView? = null

	private val soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess
	private val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	private val soundsDataAccess = SoundboardApplication.soundsDataAccess
	private val soundsDataStorage = SoundboardApplication.soundsDataStorage

	companion object {
		private val TAG = LoadLayoutDialog::class.java.name

		fun showInstance(manager: FragmentManager)
		{
			val dialog = LoadLayoutDialog()
			dialog.show(manager, TAG)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_load_sound_sheets, null)

		this.directories = (view.findViewById(R.id.rv_dialog) as RecyclerView).apply {
			this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			this.layoutManager = LinearLayoutManager(this.context)
			this.itemAnimator = DefaultItemAnimator()
		}
		this.directories?.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE)
		if (previousPath != null)
			super.adapter.setParent(File(previousPath))

		return AlertDialog.Builder(this.activity).apply {
			this.setView(view)
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss() })
			this.setPositiveButton(R.string.dialog_load, { dialogInterface, i -> onConfirm() })
		}.create()
	}

	override fun onFileSelected(selectedFile: File)
	{
		val position = super.adapter.fileList.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun canSelectMultipleFiles(): Boolean = false

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	private fun onConfirm()
	{
		val currentDirectory = super.adapter.parentFile
		if (currentDirectory != null)
			this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.path)

		if (super.adapter.selectedFiles.size != 0)
			this.loadFromFileAndDismiss(super.adapter.selectedFiles.iterator().next())
		else
			Toast.makeText(this.activity, R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show()
	}

	private fun loadFromFileAndDismiss(file: File)
	{
		try
		{
			val parsedJson = readFromFile(file)

			val soundSheets = parsedJson.soundSheets
			val playList = parsedJson.playList
			val sounds = parsedJson.sounds

			this.addLoadedSoundSheets(soundSheets)

			this.addLoadedSounds(sounds)
			this.addLoadedPlayList(playList)

			this.dismiss()
		} catch (e: IOException)
		{
			e.printStackTrace()
			SoundboardApplication.reportError(e)
		}
	}

	private fun addLoadedSoundSheets(newSoundSheets: List<SoundSheet>)
	{
		val oldCurrentSoundSheet = this.soundSheetsDataAccess.getSoundSheets()

		val playersToRemove = ArrayList<MediaPlayerController>()
		for (soundSheet in oldCurrentSoundSheet)
			playersToRemove.addAll(this.soundsDataAccess.getSoundsInFragment(soundSheet.fragmentTag))

		this.soundsDataStorage.removeSounds(playersToRemove)
		this.soundSheetsDataStorage.removeSoundSheets(oldCurrentSoundSheet)

		for (soundSheet in newSoundSheets)
			this.soundSheetsDataStorage.addSoundSheetToManager(soundSheet)
	}

	private fun addLoadedPlayList(playList: List<MediaPlayerData>)
	{
		this.soundsDataStorage.removeSoundsFromPlaylist(this.soundsDataAccess.playlist) // clear playlist before adding new values

		for (mediaPlayerData in playList)
			this.soundsDataStorage.createPlaylistSoundAndAddToManager(mediaPlayerData)
	}

	private fun addLoadedSounds(sounds: Map<String, List<MediaPlayerData>>)
	{
		for (key in sounds.keys) {
			val soundsPerFragment = sounds[key].orEmpty()
			for (mediaPlayerData in soundsPerFragment)
				this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData)
		}
	}

}
