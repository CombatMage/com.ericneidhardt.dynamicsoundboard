package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
public class SoundSheetViewHolder
(
		itemView: View,
		private val onItemClickListener: SoundSheetViewHolder.OnItemClickListener
) : RecyclerView.ViewHolder(itemView)
{
	private val label = itemView.findViewById(R.id.tv_label) as TextView
	private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
	private val soundCount = itemView.findViewById(R.id.tv_sound_count) as TextView
	private val soundCountLabel = itemView.findViewById(R.id.tv_sound_count_label)


	private var data: SoundSheet? = null

	init {
		itemView.setOnClickListener({ view
			-> this.onItemClickListener.onItemClick(this.data as SoundSheet) })
	}

	public fun bindData(data: SoundSheet, soundCount: Int)
	{
		this.data = data

		this.label.setText(data.getLabel())
		this.setSoundCount(soundCount)

		this.label.setSelected(data.getIsSelected())
		this.selectionIndicator.setVisibility(if (data.getIsSelected()) View.VISIBLE else View.INVISIBLE)

		this.label.setActivated(data.getIsSelectedForDeletion())
		this.itemView.setSelected(data.getIsSelectedForDeletion())

	}

	private fun setSoundCount(soundCount: Int)
	{
		if (soundCount == 0) {
			this.soundCount.setVisibility(View.INVISIBLE)
			this.soundCountLabel.setVisibility(View.INVISIBLE)
		} else {
			this.soundCountLabel.setVisibility(View.VISIBLE)
			this.soundCount.setVisibility(View.VISIBLE)
			this.soundCount.setText(Integer.toString(soundCount))
		}
	}

	public interface OnItemClickListener
	{
		fun onItemClick(data: SoundSheet)
	}

}

