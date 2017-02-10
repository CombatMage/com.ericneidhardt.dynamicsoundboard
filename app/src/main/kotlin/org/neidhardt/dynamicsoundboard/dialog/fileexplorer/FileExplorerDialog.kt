package org.neidhardt.dynamicsoundboard.dialog.fileexplorer

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
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.containsAudioFiles
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectorySorted
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.utils.longHash
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
abstract class FileExplorerDialog : BaseDialog() {

	private val KEY_PARENT_FILE = "FileExplorerDialog.KEY_PARENT_FILE"

	internal val adapter: DirectoryAdapter = DirectoryAdapter()

	protected abstract fun canSelectDirectory(): Boolean

	protected abstract fun canSelectFile(): Boolean

	protected abstract fun canSelectMultipleFiles(): Boolean

	protected abstract fun onFileSelected(selectedFile: File)

	fun storePathToSharedPreferences(key: String, path: String) {
		val context = SoundboardApplication.context
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		val editor = preferences.edit()
		editor.putString(key, path)
		editor.apply()
	}

	fun getPathFromSharedPreferences(key: String): String? {
		val context = SoundboardApplication.context
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getString(key, null)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		if (savedInstanceState != null) {
			val parentFilePath = savedInstanceState.getString(KEY_PARENT_FILE)
			if (parentFilePath != null)
				this.adapter.setParent(File(parentFilePath))
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString(KEY_PARENT_FILE, this.adapter.parentFile!!.path)
	}

	internal inner class DirectoryAdapter : RecyclerView.Adapter<DirectoryEntry>() {

		internal var parentFile: File? = null
		internal var selectedEntries: MutableSet<DirectoryEntry> = HashSet()
		internal var selectedFiles: MutableSet<File> = HashSet()

		internal val fileList: List<File> get() = mFileList
		internal val mFileList: MutableList<File> = ArrayList()

		init {
			this.setHasStableIds(true)
			this.setParent(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
		}

		fun setParent(parent: File) {
			this.parentFile = parent
			this.mFileList.clear()
			if (parent.parentFile != null) {
				this.mFileList.add(0, parent.parentFile)
				this.notifyDataSetChanged()
			}

			subscriptions.add(parent.getFilesInDirectorySorted()
					.delay(200, TimeUnit.MILLISECONDS)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe { filesUnderParent ->
						this.mFileList.addAll(filesUnderParent)
						this.notifyItemRangeInserted(1, filesUnderParent.size)
					})
		}

		// refresh current view by resetting the current parent. This will update the file list
		fun refreshDirectory() {
			this.parentFile?.let { currentParent ->
				this.setParent(currentParent)
			}
		}

		override fun getItemCount(): Int = this.fileList.size

		override fun getItemId(position: Int): Long = this.fileList[position].absolutePath.longHash

		override fun onCreateViewHolder(parent: ViewGroup, i: Int): DirectoryEntry {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.view_directory_item, parent, false)
			return DirectoryEntry(view)
		}

		override fun onBindViewHolder(directoryEntry: DirectoryEntry, position: Int) {
			val file = this.fileList[position]
			directoryEntry.bindData(file)
		}
	}

	internal inner class DirectoryEntry(itemView: View) :
			RecyclerView.ViewHolder(itemView),
			View.OnClickListener,
			View.OnLongClickListener,
			Animator.AnimatorListener
	{
		private var file: File? = null
		private val fileType = itemView.findViewById(R.id.iv_file_type) as ImageView
		private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
		private val fileName = itemView.findViewById(R.id.tv_label) as TextView

		init {
			itemView.setOnClickListener(this)
			itemView.setOnLongClickListener(this)
		}

		fun bindData(file: File) {
			this.file = file
			if (file == adapter.parentFile!!.parentFile)
				this.bindParentDirectory()
			else {
				this.fileName.text = file.name
				if (file.isDirectory)
					this.bindDirectory(file)
				else
					this.bindFile(file)

				val isEntrySelected = adapter.selectedFiles.contains(file)
				this.setSelection(isEntrySelected)
				if (isEntrySelected) {
					adapter.selectedEntries.add(this)
				}
			}
		}

		private fun setSelection(selected: Boolean) {
			this.selectionIndicator.visibility = if (selected) View.VISIBLE else View.INVISIBLE
			this.fileType.isSelected = selected
			this.fileName.isSelected = selected
		}

		private fun bindFile(file: File) {
			if (file.isAudioFile)
				this.fileType.setImageResource(R.drawable.selector_ic_file_sound)
			else
				this.fileType.setImageResource(R.drawable.selector_ic_file)
		}

		private fun bindDirectory(file: File) {
			if (file.containsAudioFiles)
				this.fileType.setImageResource(R.drawable.selector_ic_folder_sound)
			else
				this.fileType.setImageResource(R.drawable.selector_ic_folder)
		}

		private fun bindParentDirectory() {
			this.fileName.text = ".."
			this.fileType.setImageResource(R.drawable.selector_ic_parent_directory)
			this.selectionIndicator.visibility = View.GONE
		}

		override fun onClick(v: View) {
			this.file?.let { file ->
				if (!file.isDirectory)
					return
				adapter.setParent(file)
			}
		}

		override fun onLongClick(v: View): Boolean {
			val file = this.file ?: return false
			if (file == adapter.parentFile!!.parentFile)
				return false

			if (adapter.selectedFiles.contains(file)) {
				adapter.selectedFiles.remove(file)
				adapter.selectedEntries.remove(this)
				this.setSelection(false)
				return false
			}

			if (file.isDirectory && !canSelectDirectory())
				return false

			if (!file.isDirectory && !canSelectFile())
				return false

			this.selectEntry(file)

			return true
		}

		private fun selectEntry(file: File) {
			if (canSelectMultipleFiles()) {
				adapter.selectedFiles.add(file)
				adapter.selectedEntries.add(this)
			}
			else {
				adapter.selectedFiles.clear()
				adapter.selectedFiles.add(file)

				adapter.selectedEntries.map { entry -> entry.setSelection(false) }
				adapter.selectedEntries.clear()
				adapter.selectedEntries.add(this)
			}

			this.setSelection(true)
			this.animateSelectorSlideIn()
			this.animateFileLogoRotate()

			onFileSelected(file)
		}

		private fun animateFileLogoRotate() {
			this.fileType.animate().withLayer()
					.rotationYBy(360f)
					.setDuration(resources
					.getInteger(android.R.integer.config_mediumAnimTime)
					.toLong())
					.setListener(this)
					.start()
		}

		override fun onAnimationStart(animation: Animator) {}

		override fun onAnimationEnd(animation: Animator) {
			this.fileType.rotationY = 0f
		}

		override fun onAnimationCancel(animation: Animator) {
			this.fileType.rotationY = 0f
		}

		override fun onAnimationRepeat(animation: Animator) {}

		private fun animateSelectorSlideIn() {
			val distance = this.selectionIndicator.width
			this.selectionIndicator.translationX = distance.toFloat() // move selector to the right to be out of the screen

			this.selectionIndicator.animate().withLayer().
					translationX(0f)
					.setDuration(resources
					.getInteger(android.R.integer.config_mediumAnimTime)
					.toLong())
					.setInterpolator(DecelerateInterpolator())
					.start()
		}
	}
}
