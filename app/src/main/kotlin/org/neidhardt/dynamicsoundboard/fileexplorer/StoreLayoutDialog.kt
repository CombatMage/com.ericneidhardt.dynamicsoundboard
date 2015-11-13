package org.neidhardt.dynamicsoundboard.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.JsonPojo
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration
import java.io.File
import java.io.IOException

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public class StoreLayoutDialog : FileExplorerDialog(), LayoutStorageDialog, View.OnClickListener
{

	private var inputFileName: EditText? = null
	private var confirm: View? = null
	private var directories: RecyclerView? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_store_sound_sheets, null)
		view.findViewById(R.id.b_add).setOnClickListener(this)
		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		this.confirm = view.findViewById(R.id.b_ok)
		this.confirm!!.setOnClickListener(this)
		this.confirm!!.isEnabled = false

		this.inputFileName = view.findViewById(R.id.et_name_file) as EditText

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

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	override fun canSelectMultipleFiles(): Boolean = false

	override fun onFileSelected(selectedFile: File)
	{
		this.confirm!!.isEnabled = true
		val position = super.adapter.fileList.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun onClick(v: View)
	{
		when (v.id)
		{
			R.id.b_add -> {
				this.createFileAndSelect()
				this.hideKeyboard()
			}
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok -> {
				val currentDirectory = super.adapter.parentFile
				if (currentDirectory != null)
					this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.path)

				if (super.adapter.selectedFiles.size != 0)
					this.saveDataAndDismiss()
				else
					Toast.makeText(this.activity, R.string.dialog_store_layout_no_file_info, Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun hideKeyboard()
	{
		if (this.inputFileName!!.hasFocus() && this.activity != null)
		{
			val inputManager = this.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputManager.hideSoftInputFromWindow(this.inputFileName!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
		}
	}

	private fun createFileAndSelect()
	{
		val fileName = this.inputFileName!!.text.toString()
		if (fileName.isEmpty())
		{
			Toast.makeText(this.activity, R.string.dialog_store_layout_no_file_name, Toast.LENGTH_SHORT).show()
			return
		}

		val file = File(super.adapter.parentFile, fileName)
		if (file.exists())
		{
			Toast.makeText(this.activity, R.string.dialog_store_layout_file_exists, Toast.LENGTH_SHORT).show()
			return
		}

		try
		{
			val created = file.createNewFile()
			if (!created) {
				Toast.makeText(this.activity, R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
				return
			}

			super.adapter.selectedFiles.add(file)
			super.adapter.refreshDirectory()
			super.adapter.notifyDataSetChanged()

			this.onFileSelected(file)
		}
		catch (e: IOException)
		{
			Toast.makeText(this.activity, R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
		}

	}

	private fun saveDataAndDismiss()
	{
		try
		{
			JsonPojo.writeToFile(
					super.adapter.selectedFiles.elementAt(0),
					this.soundSheetsDataAccess.getSoundSheets(),
					this.soundsDataAccess.playlist,
					this.soundsDataAccess.sounds)

			this.dismiss()
		}
		catch (e: IOException)
		{
			Logger.d(TAG, e.getMessage())
			Toast.makeText(this.activity, R.string.dialog_store_layout_failed_store_layout, Toast.LENGTH_SHORT).show()
		}


	}

	companion object
	{
		private val TAG = StoreLayoutDialog::class.java.name

		public fun showInstance(manager: FragmentManager)
		{
			val dialog = StoreLayoutDialog()
			dialog.show(manager, TAG)
		}
	}

}
