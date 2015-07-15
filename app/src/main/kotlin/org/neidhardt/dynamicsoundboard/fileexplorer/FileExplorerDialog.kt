package org.neidhardt.dynamicsoundboard.fileexplorer

import android.animation.Animator
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.views.BaseDialog
import java.io.File
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public abstract class FileExplorerDialog : BaseDialog()
{
	private val KEY_PARENT_FILE = "org.neidhardt.dynamicsoundboard.fileexplorer.parentFile"

	public val adapter: DirectoryAdapter = DirectoryAdapter()

	protected abstract fun canSelectDirectory(): Boolean

	protected abstract fun canSelectFile(): Boolean

	protected abstract fun onFileSelected()

	public fun storePathToSharedPreferences(key: String, path: String)
	{
		val context = DynamicSoundboardApplication.getSoundboardContext()
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		val editor = preferences.edit()
		editor.putString(key, path)
		editor.apply()
	}

	public fun getPathFromSharedPreferences(key: String): String?
	{
		val context = DynamicSoundboardApplication.getSoundboardContext()
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getString(key, null)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		if (savedInstanceState != null)
		{
			val parentFilePath = savedInstanceState.getString(KEY_PARENT_FILE)
			if (parentFilePath != null)
				this.adapter.setParent(File(parentFilePath))
		}
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		super.onSaveInstanceState(outState)
		outState.putString(KEY_PARENT_FILE, this.adapter.parentFile!!.getPath())
	}

	public inner class DirectoryAdapter : RecyclerView.Adapter<DirectoryEntry>()
	{
		internal var parentFile: File? = null
		internal var selectedEntry: DirectoryEntry? = null
		internal var selectedFile: File? = null

		internal var fileList: MutableList<File> = ArrayList()

		init
		{
			this.setParent(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
			this.notifyDataSetChanged()
		}

		public fun setParent(parent: File)
		{
			this.parentFile = parent
			this.fileList = FileUtils.getFilesInDirectory(this.parentFile)
			if (parent.getParentFile() != null)
				this.fileList.add(0, parent.getParentFile())
		}

		public fun refreshDirectory()
		{
			this.fileList = FileUtils.getFilesInDirectory(this.parentFile)
			if (this.parentFile!!.getParentFile() != null)
				this.fileList.add(0, this.parentFile!!.getParentFile())
		}

		override fun onCreateViewHolder(parent: ViewGroup, i: Int): DirectoryEntry
		{
			val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_directory_item, parent, false)
			return DirectoryEntry(view)
		}

		override fun onBindViewHolder(directoryEntry: DirectoryEntry, position: Int)
		{
			val file = this.fileList.get(position)
			directoryEntry.bindData(file)
		}

		override fun getItemCount(): Int
		{
			return this.fileList.size()
		}
	}

	internal inner class DirectoryEntry(itemView: View) :
			RecyclerView.ViewHolder(itemView),
			View.OnClickListener,
			View.OnLongClickListener,
			Animator.AnimatorListener
	{
		private val fileType = itemView.findViewById(R.id.iv_file_type) as ImageView
		private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
		private val fileName = itemView.findViewById(R.id.tv_label) as TextView

		init
		{
			itemView.setOnClickListener(this)
			itemView.setOnLongClickListener(this)
		}

		public fun bindData(file: File)
		{
			if (file == adapter.parentFile!!.getParentFile())
				this.bindParentDirectory()
			else
			{
				this.fileName.setText(file.getName())
				if (file.isDirectory())
					this.bindDirectory(file)
				else
					this.bindFile(file)

				val isEntrySelected = file == adapter.selectedFile
				this.setSelection(isEntrySelected)
				if (isEntrySelected)
					adapter.selectedEntry = this
			}
		}

		private fun setSelection(selected: Boolean)
		{
			this.selectionIndicator.setVisibility(if (selected) View.VISIBLE else View.INVISIBLE)
			this.fileType.setSelected(selected)
			this.fileName.setSelected(selected)
		}

		private fun bindFile(file: File)
		{
			if (FileUtils.isAudioFile(file))
				this.fileType.setImageResource(R.drawable.selector_ic_file_sound)
			else
				this.fileType.setImageResource(R.drawable.selector_ic_file)
		}

		private fun bindDirectory(file: File)
		{
			if (FileUtils.containsAudioFiles(file))
				this.fileType.setImageResource(R.drawable.selector_ic_folder_sound)
			else
				this.fileType.setImageResource(R.drawable.selector_ic_folder)
		}

		private fun bindParentDirectory()
		{
			this.fileName.setText("..")
			this.fileType.setImageResource(R.drawable.selector_ic_parent_directory)
			this.selectionIndicator.setVisibility(View.GONE)
		}

		override fun onClick(v: View)
		{
			val file = adapter.fileList.get(this.getLayoutPosition())
			if (!file.isDirectory())
				return
			adapter.setParent(file)
			adapter.notifyDataSetChanged()
		}

		override fun onLongClick(v: View): Boolean
		{
			val file = adapter.fileList.get(this.getLayoutPosition())
			if (file == adapter.parentFile!!.getParentFile())
				return false

			if (file.isDirectory() && !canSelectDirectory())
				return false

			if (!file.isDirectory() && !canSelectFile())
				return false

			this.selectEntry(file)

			return true
		}

		private fun selectEntry(file: File)
		{
			adapter.selectedFile = file
			if (adapter.selectedEntry != null)
				adapter.selectedEntry!!.setSelection(false)

			adapter.selectedEntry = this

			this.setSelection(true)
			this.animateSelectorSlideIn()
			this.animateFileLogoRotate()

			onFileSelected()
		}

		private fun animateFileLogoRotate()
		{
			this.fileType.animate().rotationYBy(360f)
					.setDuration(getResources()
					.getInteger(android.R.integer.config_mediumAnimTime)
					.toLong())
					.setListener(this)
					.start()
		}

		override fun onAnimationStart(animation: Animator) {}

		override fun onAnimationEnd(animation: Animator) { this.fileType.setRotationY(0f) }

		override fun onAnimationCancel(animation: Animator) { this.fileType.setRotationY(0f) }

		override fun onAnimationRepeat(animation: Animator) {}

		private fun animateSelectorSlideIn()
		{
			val distance = this.selectionIndicator.getWidth()
			this.selectionIndicator.setTranslationX(distance.toFloat()) // move selector to the right to be out of the screen

			this.selectionIndicator.animate().translationX(0f)
					.setDuration(getResources()
					.getInteger(android.R.integer.config_mediumAnimTime)
					.toLong())
					.setInterpolator(DecelerateInterpolator())
					.start()
		}
	}

}
