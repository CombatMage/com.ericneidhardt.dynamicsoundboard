package org.neidhardt.dynamicsoundboard.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.FragmentManager
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.JsonPojo
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public class LoadLayoutDialog : FileExplorerDialog(), LayoutStorageDialog, View.OnClickListener
{
	private var confirm: View? = null
	private var directories: RecyclerView? = null

	companion object {
		private val TAG = LoadLayoutDialog::class.java.name

		public fun showInstance(manager: FragmentManager)
		{
			val dialog = LoadLayoutDialog()
			dialog.show(manager, TAG)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_load_sound_sheets, null)
		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		this.confirm = view.findViewById(R.id.b_ok)
		this.confirm!!.setOnClickListener(this)
		this.confirm!!.isEnabled = false

		this.directories = view.findViewById(R.id.rv_dialog) as RecyclerView
		this.directories!!.addItemDecoration(DividerItemDecoration())
		this.directories!!.layoutManager = LinearLayoutManager(this.activity)
		this.directories!!.itemAnimator = DefaultItemAnimator()
		this.directories!!.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE)
		if (previousPath != null)
			super.adapter.setParent(File(previousPath))

		val dialog = AppCompatDialog(this.activity, R.style.DialogThemeNoTitle)
		dialog.setContentView(view)

		return dialog
	}

	override fun onFileSelected(selectedFile: File)
	{
		this.confirm!!.isEnabled = true
		val position = super.adapter.fileList.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun canSelectMultipleFiles(): Boolean = false

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	override fun onClick(v: View)
	{
		when (v.id) {
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok ->
			{
				val currentDirectory = super.adapter.parentFile
				if (currentDirectory != null)
					this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.path)

				if (super.adapter.selectedFiles.size != 0)
					this.loadFromFileAndDismiss(super.adapter.selectedFiles.iterator().next())
				else
					Toast.makeText(this.activity, R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun loadFromFileAndDismiss(file: File)
	{
		try
		{
			val parsedJson = JsonPojo.readFromFile(file)

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
			DynamicSoundboardApplication.reportError(e)
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
