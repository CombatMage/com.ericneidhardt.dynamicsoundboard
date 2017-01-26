package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
class SoundSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val label = itemView.findViewById(R.id.tv_label) as TextView
	private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
	private val soundCount = itemView.findViewById(R.id.tv_sound_count) as TextView
	private val soundCountLabel = itemView.findViewById(R.id.tv_sound_count_label)
	private val divider = itemView.findViewById(R.id.v_divider)

	var data: NewSoundSheet? = null

	fun bindData(data: NewSoundSheet, isLastItem: Boolean) {
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

