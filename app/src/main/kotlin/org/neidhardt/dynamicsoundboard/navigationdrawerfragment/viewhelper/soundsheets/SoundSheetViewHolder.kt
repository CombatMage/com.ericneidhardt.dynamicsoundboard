package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundsheets

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_sound_sheet_item.view.*
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
class SoundSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val label = itemView.tv_label
	private val selectionIndicator = itemView.iv_selected
	private val soundCount = itemView.tv_sound_count
	private val soundCountLabel = itemView.tv_sound_count_label
	private val divider = itemView.v_divider

	var data: SoundSheet? = null

	fun bindData(data: SoundSheet, isLastItem: Boolean) {
		this.data = data

		this.label.text = data.label
		this.setSoundCount(data.mediaPlayers?.size ?: 0)

		this.label.isSelected = data.isSelected
		this.selectionIndicator.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE

		this.label.isActivated = data.isSelectedForDeletion
		this.itemView.isSelected = data.isSelectedForDeletion

		this.divider.visibility = if (isLastItem) View.INVISIBLE else View.VISIBLE
	}

	private fun setSoundCount(soundCount: Int) {
		if (soundCount == 0) {
			this.soundCount.visibility = View.INVISIBLE
			this.soundCountLabel.visibility = View.INVISIBLE
		} else {
			this.soundCountLabel.visibility = View.VISIBLE
			this.soundCount.visibility = View.VISIBLE
			this.soundCount.text = soundCount.toString()
		}
	}

}

