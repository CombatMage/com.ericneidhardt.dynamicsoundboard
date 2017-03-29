package org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base

import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectorySorted
import org.neidhardt.dynamicsoundboard.misc.getFilesInDirectorySortedAsync
import org.neidhardt.utils.Tuple
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

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
				this.setStartDirectoryForAdapter(java.io.File(path))
			}
		}

		this.adapter.clicksFileEntry
				.bindToLifecycle(this)
				.filter(File::isDirectory)
				.doOnNext { dir ->
					this.displayRootDirectory(dir)
					this.adapter.currentDirectory = dir
				}
				.subscribeOn(Schedulers.computation())
				.map { dir -> Tuple<File?, List<File>>(dir.parentFile, dir.getFilesInDirectorySorted()) }
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { tupleRootFiles -> this.displayFilesInDirectory(tupleRootFiles.first, tupleRootFiles.second) }

		this.adapter.selectsFileEntry
				.bindToLifecycle(this)
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
				}
	}

	private fun displayRootDirectory(directory: File) {
		if (!directory.isDirectory) throw IllegalArgumentException()
		if (this.adapter.displayedFiles.isEmpty()) throw IllegalStateException()

		val externalFileStorage = Environment.getExternalStorageDirectory()
		val rootDirectory = directory.parentFile

		// clicked directory is already root ( / ) our outside of external storage path
		if (rootDirectory == null
				|| rootDirectory.absolutePath.length < externalFileStorage.absolutePath.length) {
			this.adapter.rootDirectory = null
			this.adapter.displayedFiles.removeAt(0)
			this.adapter.notifyItemRemoved(0)
		}
		else {
			this.adapter.rootDirectory = rootDirectory
			this.adapter.displayedFiles[0] = rootDirectory
			this.adapter.notifyItemChanged(0)
		}
	}

	private fun displayFilesInDirectory(root: File?, files: List<File>) {
		this.adapter.displayedFiles.clear()
		if (root == null) {
			this.adapter.displayedFiles.addAll(files)
		}
		else {
			this.adapter.displayedFiles.add(root)
			this.adapter.displayedFiles.addAll(files)
		}
		this.adapter.notifyDataSetChanged()
	}

	private fun canBeFileSelected(file: File?): Boolean {
		if (file == null) return false
		return file.isDirectory && this.canSelectDirectory()
				|| !file.isDirectory && this.canSelectFile()
	}

	protected fun setStartDirectoryForAdapter(directory: File) {
		// set link to up directory if available
		val externalFileStorage = Environment.getExternalStorageDirectory()
		val rootDirectory = directory.parentFile

		if (rootDirectory != null) {
			val externalFileStoragePath = externalFileStorage.absolutePath.split('/')
			val rootDirectoryPath = rootDirectory.absolutePath.split('/')

			if (externalFileStoragePath.size < rootDirectoryPath.size) {
				this.adapter.rootDirectory = rootDirectory
				this.adapter.displayedFiles.add(rootDirectory)
				this.adapter.notifyItemInserted(0)
			}
		}

		this.adapter.currentDirectory = directory
		this.displayFilesInDirAsync(directory)
	}

	protected fun addFileToAdapter(file: File) {
		this.adapter.displayedFiles.add(file)
		this.adapter.notifyItemInserted(this.adapter.displayedFiles.size - 1)
	}

	private fun displayFilesInDirAsync(directory: File) {
		// request files in directory and add them
			directory.getFilesInDirectorySortedAsync()
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe { filesUnderParent ->
						val startIndex = this.adapter.displayedFiles.size
						this.adapter.displayedFiles.addAll(filesUnderParent)
						this.adapter.notifyItemRangeInserted(startIndex, filesUnderParent.size)
					}
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
