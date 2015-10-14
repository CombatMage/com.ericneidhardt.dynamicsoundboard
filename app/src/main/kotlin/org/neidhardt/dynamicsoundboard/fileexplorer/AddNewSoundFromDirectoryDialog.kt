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
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromFileListTask
import org.neidhardt.dynamicsoundboard.views.BaseDialog
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

import java.io.File
import java.util.ArrayList

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public open class AddNewSoundFromDirectoryDialog : FileExplorerDialog(), View.OnClickListener
{
	protected var callingFragmentTag: String? = null

	private var confirm: View? = null
	private var directories: RecyclerView? = null

	companion object
	{
		private val TAG = AddNewSoundFromDirectoryDialog::class.java.name

		public fun showInstance(manager: FragmentManager, callingFragmentTag: String)
		{
			val dialog = AddNewSoundFromDirectoryDialog()

			val args = Bundle()
			args.putString(dialog.KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
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
		this.confirm = view.findViewById(R.id.b_ok)
		this.confirm!!.setOnClickListener(this)
		this.confirm!!.isEnabled = false

		view.findViewById(R.id.b_cancel).setOnClickListener(this)

		this.directories = view.findViewById(R.id.rv_dialog) as RecyclerView
		this.directories!!.addItemDecoration(DividerItemDecoration())
		this.directories!!.layoutManager = LinearLayoutManager(this.activity)
		this.directories!!.itemAnimator = DefaultItemAnimator()
		this.directories!!.adapter = super.adapter

		val previousPath = this.getPathFromSharedPreferences(TAG)
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

	override fun canSelectDirectory(): Boolean = true

	override fun canSelectFile(): Boolean = true

	override fun canSelectMultipleFiles(): Boolean = true

	override fun onClick(v: View)
	{
		if (v.id == R.id.b_cancel)
			this.dismiss()
		else if (v.id == R.id.b_ok)
		{
			val currentDirectory = super.adapter.parentFile
			if (currentDirectory != null)
				this.storePathToSharedPreferences(TAG, currentDirectory.path)

			this.returnResults()
			this.dismiss()
		}
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
				val filesInSelectedDir = file.listFiles()
				if (filesInSelectedDir != null) {
					for (fileInDir in filesInSelectedDir)
					{
						if (!files.contains(fileInDir))
							files.add(fileInDir)
					}
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
