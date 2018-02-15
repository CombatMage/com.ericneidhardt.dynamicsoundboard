package org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.subjects.PublishSubject
import org.neidhardt.androidutils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.app_utils.longHash
import java.io.File
import java.util.*

/**
 * Created by eric.neidhardt@gmail.com on 13.02.2017.
 */
class DirectoryAdapter : BaseAdapter<File, FileViewHolder>() {

	internal val clicksFileEntry: PublishSubject<File> = PublishSubject.create()
	internal val selectsFileEntry: PublishSubject<FileViewHolder> = PublishSubject.create()

	internal var currentDirectory: File? = null
	internal var rootDirectory: File? = null
	internal val displayedFiles: MutableList<File> get() = this.values
	internal val selectedFiles: MutableSet<File> = HashSet()

	override val values: MutableList<File> = ArrayList()

	init { this.setHasStableIds(true) }

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].absolutePath.longHash

	override fun onCreateViewHolder(parent: ViewGroup, i: Int): FileViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.view_directory_item, parent, false)
		val viewHolder = FileViewHolder(view)

		val parentDetaches = RxView.detaches(parent)

		RxView.clicks(view)
				.takeUntil(parentDetaches)
				.filter { viewHolder.file != null }
				.map { viewHolder.file!! }
				.subscribe(this.clicksFileEntry)

		RxView.longClicks(view)
				.takeUntil(parentDetaches)
				.map { viewHolder }
				.subscribe(this.selectsFileEntry)

		return viewHolder
	}

	override fun onBindViewHolder(directoryEntry: FileViewHolder, position: Int) {
		val file = this.values[position]
		directoryEntry.bindData(
				file = file,
				isRootDir = file == this.rootDirectory,
				isSelected = this.selectedFiles.contains(file))
	}
}