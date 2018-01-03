package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundlayouts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_sound_layout_item.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.model.SoundLayout

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
class SoundLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val label: TextView = itemView.tv_view_sound_layout_item_name
	private val selectionIndicator: ImageView = itemView.iv_view_sound_layout_item_selected
	private val divider = itemView.v_divider

	val openSettings: View = itemView.ib_view_sound_layout_item_settings

	var data: SoundLayout? = null

	fun bindData(data: SoundLayout, isLastItem: Boolean) {
		this.data = data

		this.label.text = data.label
		this.label.isSelected = data.isSelected
		this.selectionIndicator.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE

		this.label.isActivated = data.isSelectedForDeletion
		this.itemView.isSelected = data.isSelectedForDeletion

		this.divider.visibility = if (isLastItem) View.INVISIBLE else View.VISIBLE
	}
}