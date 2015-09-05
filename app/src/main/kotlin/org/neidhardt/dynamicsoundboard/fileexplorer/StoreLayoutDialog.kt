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
import android.widget.Toast
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.JsonPojo
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.views.edittext.NoUnderscoreEditText
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

import java.io.File
import java.io.IOException

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public class StoreLayoutDialog : FileExplorerDialog(), LayoutStorageDialog, View.OnClickListener
{

	private var inputFileName: NoUnderscoreEditText? = null
	private var confirm: View? = null
	private var directories: RecyclerView? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_store_sound_sheets, null)
		view.findViewById(R.id.b_add).setOnClickListener(this)
		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		this.confirm = view.findViewById(R.id.b_ok)
		this.confirm!!.setOnClickListener(this)
		this.confirm!!.setEnabled(false)

		this.inputFileName = view.findViewById(R.id.et_name_file) as NoUnderscoreEditText

		this.directories = view.findViewById(R.id.rv_dialog) as RecyclerView
		this.directories!!.addItemDecoration(DividerItemDecoration())
		this.directories!!.setLayoutManager(LinearLayoutManager(this.getActivity()))
		this.directories!!.setItemAnimator(DefaultItemAnimator())
		this.directories!!.setAdapter(super<FileExplorerDialog>.adapter)

		val previousPath = this.getPathFromSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE)
		if (previousPath != null)
			super<FileExplorerDialog>.adapter.setParent(File(previousPath))

		val dialog = AppCompatDialog(this.getActivity(), R.style.DialogThemeNoTitle)
		dialog.setContentView(view)

		return dialog
	}

	override fun onFileSelected()
	{
		this.confirm!!.setEnabled(true)
		val position = super<FileExplorerDialog>.adapter.fileList.indexOf(super<FileExplorerDialog>.adapter.selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun canSelectDirectory(): Boolean = false

	override fun canSelectFile(): Boolean = true

	override fun onClick(v: View)
	{
		when (v.getId())
		{
			R.id.b_add -> {
				this.createFileAndSelect()
				this.hideKeyboard()
			}
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok -> {
				val currentDirectory = super<FileExplorerDialog>.adapter.parentFile
				if (currentDirectory != null)
					this.storePathToSharedPreferences(LayoutStorageDialog.KEY_PATH_STORAGE, currentDirectory.getPath())

				if (super<FileExplorerDialog>.adapter.selectedFile != null)
					this.saveDataAndDismiss()
				else
					Toast.makeText(this.getActivity(), R.string.dialog_store_layout_no_file_info, Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun hideKeyboard()
	{
		if (this.inputFileName!!.hasFocus() && this.getActivity() != null)
		{
			val inputManager = this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputManager.hideSoftInputFromWindow(this.inputFileName!!.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
		}
	}

	private fun createFileAndSelect()
	{
		val fileName = this.inputFileName!!.getText().toString()
		if (fileName.isEmpty())
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_no_file_name, Toast.LENGTH_SHORT).show()
			return
		}

		val file = File(super<FileExplorerDialog>.adapter.parentFile, fileName)
		if (file.exists())
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_file_exists, Toast.LENGTH_SHORT).show()
			return
		}

		try
		{
			val created = file.createNewFile()
			if (!created) {
				Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
				return
			}

			super<FileExplorerDialog>.adapter.selectedFile = file
			super<FileExplorerDialog>.adapter.refreshDirectory()
			super<FileExplorerDialog>.adapter.notifyDataSetChanged()

			this.onFileSelected()
		}
		catch (e: IOException)
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show()
		}

	}

	private fun saveDataAndDismiss()
	{
		try
		{
			JsonPojo.writeToFile(
					super<FileExplorerDialog>.adapter.selectedFile,
					this.soundSheetsDataAccess.getSoundSheets(),
					this.soundsDataAccess.getPlaylist(),
					this.soundsDataAccess.getSounds())

			this.dismiss()
		}
		catch (e: IOException)
		{
			Logger.d(TAG, e.getMessage())
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_store_layout, Toast.LENGTH_SHORT).show()
		}


	}

	companion object
	{
		private val TAG = javaClass<StoreLayoutDialog>().getName()

		public fun showInstance(manager: FragmentManager)
		{
			val dialog = StoreLayoutDialog()
			dialog.show(manager, TAG)
		}
	}

}
