package org.neidhardt.dynamicsoundboard.dialog.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base.FileExplorerDialog
import org.neidhardt.dynamicsoundboard.misc.Logger
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.IOException

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
class StoreLayoutDialog : FileExplorerDialog(), LayoutStorageDialog, View.OnClickListener {

	private val soundLayoutManager = SoundboardApplication.soundLayoutManager

	private var inputFileName: EditText? = null
	private var directories: RecyclerView? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_store_sound_sheets, null)
		view.findViewById(R.id.b_add).setOnClickListener(this)

		this.inputFileName = view.findViewById(R.id.et_name_file) as EditText

		this.directories = (view.findViewById(R.id.rv_dialog) as RecyclerView).apply {
			this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			this.layoutManager = LinearLayoutManager(this.context)
			this.itemAnimator = DefaultItemAnimator()
		}
		this.directories?.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE)
		if (previousPath != null)
			super.setStartDirectoryForAdapter(File(previousPath))
		else {
			val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
			super.setStartDirectoryForAdapter(file)
		}

		return AlertDialog.Builder(this.activity).apply {
			this.setView(view)
			this.setNegativeButton(R.string.all_cancel, { _, _ -> dismiss() })
			this.setPositiveButton(R.string.dialog_save, { _, _ -> onConfirm() })
		}.create()
	}

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	override fun canSelectMultipleFiles(): Boolean = false

	override fun onFileSelected(selectedFile: File) {
		val position = super.adapter.displayedFiles.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.b_add -> {
				this.createFileAndSelect()
				this.hideKeyboard()
			}
		}
	}

	private fun onConfirm() {
		val currentDirectory = super.adapter.rootDirectory
		if (currentDirectory != null)
			this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.path)

		if (super.adapter.selectedFiles.size != 0)
			this.saveDataAndDismiss()
		else
			Toast.makeText(this.activity, R.string.dialog_store_layout_no_file_info, Toast.LENGTH_SHORT).show()
	}

	private fun hideKeyboard() {
		if (this.inputFileName!!.hasFocus() && this.activity != null) {
			val inputManager = this.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputManager.hideSoftInputFromWindow(this.inputFileName!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
		}
	}

	private fun createFileAndSelect() {
		val fileName = this.inputFileName!!.text.toString()
		if (fileName.isEmpty()) {
			Toast.makeText(this.activity, R.string.dialog_store_layout_no_file_name, Toast.LENGTH_SHORT).show()
			return
		}

		val file = File(super.adapter.currentDirectory, fileName)
		if (file.exists()) {
			Toast.makeText(this.activity, R.string.dialog_store_layout_file_exists, Toast.LENGTH_SHORT).show()
			return
		}

		try {
			val created = file.createNewFile()
			if (!created) {
				Toast.makeText(this.activity, R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
				return
			}

			super.selectedFiles.add(file)
			super.addFileToAdapter(file)

			this.onFileSelected(file)
		}
		catch (e: IOException) {
			Toast.makeText(this.activity, R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
		}
	}

	private fun saveDataAndDismiss() {
		val file = super.adapter.selectedFiles.elementAt(0)
		SoundboardApplication.storage.saveToFile(file, this.soundLayoutManager.soundLayouts)
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe({
			// nothing to do
		}, { error ->
			Logger.e(TAG, error.toString())
			Toast.makeText(this.activity, R.string.dialog_store_layout_failed_store_layout,
					Toast.LENGTH_SHORT).show()
			this.dismiss()
		}, {
			this.dismiss()
		})
	}

	companion object {
		private val TAG = StoreLayoutDialog::class.java.name

		fun showInstance(manager: FragmentManager) {
			val dialog = StoreLayoutDialog()
			dialog.show(manager, TAG)
		}
	}
}
