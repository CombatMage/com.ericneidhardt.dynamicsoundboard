package org.neidhardt.dynamicsoundboard.fileexplorer

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectory
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromFileListTask
import org.neidhardt.ui_utils.recyclerview.decoration.DividerItemDecoration
import java.io.File
import java.util.*

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
open class AddNewSoundFromDirectoryDialog : FileExplorerDialog()
{
	private val soundsDataStorage = SoundboardApplication.soundsDataStorage

	protected var callingFragmentTag: String? = null

	private var directories: RecyclerView? = null

	companion object
	{
		private val TAG = AddNewSoundFromDirectoryDialog::class.java.name

		fun showInstance(manager: FragmentManager, callingFragmentTag: String)
		{
			val dialog = AddNewSoundFromDirectoryDialog()

			val args = Bundle()
			args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLING_FRAGMENT_TAG)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_add_new_sound_from_directory, null)

		this.directories = (view.findViewById(R.id.rv_dialog) as RecyclerView).apply {
			this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			this.layoutManager = LinearLayoutManager(this.context)
			this.itemAnimator = DefaultItemAnimator()
		}
		this.directories?.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(TAG)
		if (previousPath != null)
			super.adapter.setParent(File(previousPath))

		return AlertDialog.Builder(this.activity).apply {
			this.setView(view)
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss() })
			this.setPositiveButton(R.string.dialog_add, { dialogInterface, i -> onConfirm() })
		}.create()
	}

	override fun onFileSelected(selectedFile: File)
	{
		val position = super.adapter.fileList.indexOf(selectedFile)
		this.directories!!.scrollToPosition(position)
	}

	override fun canSelectDirectory(): Boolean = true

	override fun canSelectFile(): Boolean = true

	override fun canSelectMultipleFiles(): Boolean = true

	private fun onConfirm()
	{
		val currentDirectory = super.adapter.parentFile
		if (currentDirectory != null)
			this.storePathToSharedPreferences(TAG, currentDirectory.path)

		this.returnResults()
		this.dismiss()
	}

	protected fun getFileListResult(): List<File>
	{
		val files = ArrayList<File>()
		val adapter = super.adapter

		for (file in adapter.selectedFiles)
		{
			if (!file.isDirectory && !files.contains(file))
				files.add(file)
			else
			{
				val filesInSelectedDir = file.getFilesInDirectory()
				for (fileInDir in filesInSelectedDir)
				{
					if (!files.contains(fileInDir))
						files.add(fileInDir)
				}
			}
		}

		return files
	}

	protected open fun returnResults()
	{
		val fragmentToAddSounds = this.callingFragmentTag
		if (fragmentToAddSounds != null)
		{
			val result = this.getFileListResult()
			val task = LoadSoundsFromFileListTask(result, fragmentToAddSounds, this.soundsDataStorage)
			task.execute()
		}
	}
}
