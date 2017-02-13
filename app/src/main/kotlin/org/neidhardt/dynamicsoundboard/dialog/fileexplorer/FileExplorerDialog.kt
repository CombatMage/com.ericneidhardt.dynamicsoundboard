package org.neidhardt.dynamicsoundboard.dialog.fileexplorer

import android.os.Bundle
import android.preference.PreferenceManager
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base.DirectoryAdapter
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectorySorted
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectorySortedAsync
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
abstract class FileExplorerDialog : BaseDialog() {

	private val KEY_PARENT_FILE = "FileExplorerDialog.KEY_PARENT_FILE"

	protected val adapter: DirectoryAdapter = DirectoryAdapter()
	protected val selectedFiles: MutableSet<File> get() = this.adapter.selectedFiles

	protected abstract fun canSelectDirectory(): Boolean

	protected abstract fun canSelectFile(): Boolean

	protected abstract fun canSelectMultipleFiles(): Boolean

	protected abstract fun onFileSelected(selectedFile: File)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		savedInstanceState?.let { previousState ->
			previousState.getString(KEY_PARENT_FILE)?.let { path ->
				this.setDirectoryForAdapter(File(path))
			}
		}

		this.subscriptions.addAll(
				this.adapter.clicksFileEntry
						.filter { it.isDirectory }
						.doOnNext { file ->
							val rootDirectory = file.parentFile
							if (rootDirectory != null) {
								this.adapter.rootDirectory = rootDirectory
								this.adapter.displayedFiles.clear()
								this.adapter.displayedFiles.add(rootDirectory)
								this.adapter.notifyDataSetChanged()
							}
						}
						.map(File::getFilesInDirectorySorted)
						.subscribe { sortedFiles ->
							val startIndex = this.adapter.displayedFiles.size
							this.adapter.displayedFiles.addAll(sortedFiles)
							this.adapter.notifyItemRangeInserted(startIndex, sortedFiles.size)
						},

				this.adapter.selectsFileEntry
						.filter { viewHolder -> viewHolder.file != adapter.rootDirectory }
						.filter { viewHolder -> this.canBeFileSelected(viewHolder.file) }
						.subscribe { viewHolder ->
							viewHolder.file?.let { file ->
								// deselect file if already selected
								if (this.selectedFiles.contains(file)) {
									this.selectedFiles.remove(file)
									viewHolder.setSelection(false)
								}
								else {
									// add files to selected files or replace current selected file with this one
									if (canSelectMultipleFiles()) {
										this.selectedFiles.add(file)
									}
									else {
										this.selectedFiles.forEach { file -> this.adapter.notifyItemChanged(file) }
										this.selectedFiles.clear()
										this.selectedFiles.add(file)
									}
									viewHolder.setSelection(true)
									viewHolder.animateSelectorSlideIn()
									viewHolder.animateFileLogoRotate()

									this.onFileSelected(file)
								}
							}
						})
	}

	private fun canBeFileSelected(file: File?): Boolean {
		if (file == null) return false
		return file.isDirectory && this.canSelectDirectory()
				|| !file.isDirectory && this.canSelectFile()
	}

	protected fun setDirectoryForAdapter(directory: File) {
		// set link to up directory if available
		val parentOfDirectory = directory.parentFile
		if (parentOfDirectory != null) {
			this.adapter.rootDirectory = parentOfDirectory
			this.adapter.displayedFiles.add(parentOfDirectory)
			this.adapter.notifyItemInserted(0)
		}

		// request files in directory and add them
		this.subscriptions.add(
			directory.getFilesInDirectorySortedAsync()
					.delay(200, TimeUnit.MILLISECONDS)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe { filesUnderParent ->
						val startIndex = this.adapter.displayedFiles.size
						this.adapter.displayedFiles.addAll(filesUnderParent)
						this.adapter.notifyItemRangeInserted(startIndex, filesUnderParent.size)
					})
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString(KEY_PARENT_FILE, this.adapter.rootDirectory?.path)
	}

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
}
