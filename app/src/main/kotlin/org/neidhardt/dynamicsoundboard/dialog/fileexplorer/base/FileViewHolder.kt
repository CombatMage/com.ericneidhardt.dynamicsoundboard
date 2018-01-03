package org.neidhardt.dynamicsoundboard.dialog.fileexplorer.base

import android.animation.Animator
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_directory_item.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.containsAudioFiles
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import java.io.File

/**
 * Created by eric.neidhardt@gmail.com on 10.02.2017.
 */
class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Animator.AnimatorListener {

	var file: File? = null

	private val fileType = itemView.iv_file_type
	private val selectionIndicator = itemView.iv_selected
	private val fileName = itemView.tv_label

	fun bindData(file: File, isSelected: Boolean, isRootDir: Boolean) {
		this.file = file
		if (isRootDir)
			this.bindParentDirectory()
		else {
			this.fileName.text = file.name
			if (file.isDirectory)
				this.bindDirectory(file)
			else
				this.bindFile(file)

			this.setSelection(isSelected)
		}
	}

	fun setSelection(selected: Boolean) {
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

	fun animateFileLogoRotate() {
		this.fileType.animate().withLayer()
				.rotationYBy(360f)
				.setDuration(this.itemView.resources
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

	fun animateSelectorSlideIn() {
		val distance = this.selectionIndicator.width
		this.selectionIndicator.translationX = distance.toFloat() // move selector to the right to be out of the screen

		this.selectionIndicator.animate().withLayer().
				translationX(0f)
				.setDuration(this.itemView.resources
					.getInteger(android.R.integer.config_mediumAnimTime)
					.toLong())
				.setInterpolator(DecelerateInterpolator())
				.start()
	}
}