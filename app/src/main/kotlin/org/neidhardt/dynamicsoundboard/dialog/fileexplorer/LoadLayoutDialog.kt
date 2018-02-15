package org.neidhardt.dynamicsoundboard.dialog.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_add_new_sound.view.*
import org.neidhardt.androidutils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base.FileExplorerDialog
import java.io.File

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
class LoadLayoutDialog : FileExplorerDialog(), LayoutStorageDialog {

	private val storage = SoundboardApplication.storage
	private val soundLayoutManager = SoundboardApplication.soundLayoutManager

	private var directories: RecyclerView? = null

	companion object {
		private val TAG = LoadLayoutDialog::class.java.name

		fun showInstance(manager: FragmentManager) {
			val dialog = LoadLayoutDialog()
			dialog.show(manager, TAG)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_load_sound_sheets, null)

		this.directories = view.rv_dialog.apply {
			this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			this.layoutManager = LinearLayoutManager(this.context)
			this.itemAnimator = DefaultItemAnimator()
		}
		this.directories?.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE)
		val documentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

		// use previous dir if still valid, else fallback
		if (previousPath == null) {
			super.setStartDirectoryForAdapter(documentDir)
		} else {
			val previousMusicDir = File(previousPath)
			if (previousMusicDir.exists()) {
				super.setStartDirectoryForAdapter(previousMusicDir)
			} else {
				super.setStartDirectoryForAdapter(documentDir)
			}
		}

		return AlertDialog.Builder(this.activity).apply {
			this.setView(view)
			this.setNegativeButton(R.string.all_cancel, { _, _ -> dismiss() })
			this.setPositiveButton(R.string.dialog_load, { _, _ -> onConfirm() })
		}.create()
	}

	override fun onFileSelected(selectedFile: File) {
		val position = super.adapter.displayedFiles.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun canSelectMultipleFiles(): Boolean = false

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	private fun onConfirm() {
		val currentDirectory = super.adapter.rootDirectory
		if (currentDirectory != null)
			this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.path)

		if (super.adapter.selectedFiles.size != 0)
			this.loadFromFileAndDismiss(super.adapter.selectedFiles.iterator().next())
		else
			Toast.makeText(this.activity, R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show()
	}

	private fun loadFromFileAndDismiss(file: File) {
		val appData = this.storage.getFromFile(file)
		this.soundLayoutManager.init(appData)
	}
}
