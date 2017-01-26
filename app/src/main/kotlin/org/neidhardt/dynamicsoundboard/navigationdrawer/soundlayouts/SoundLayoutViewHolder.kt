package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
class SoundLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val label: TextView = itemView.findViewById(R.id.tv_view_sound_layout_item_name) as TextView
	private val selectionIndicator: ImageView = itemView.findViewById(R.id.iv_view_sound_layout_item_selected) as ImageView
	private val divider = itemView.findViewById(R.id.v_divider)

	val openSettings: View = itemView.findViewById(R.id.ib_view_sound_layout_item_settings)

	var data: NewSoundLayout? = null

	fun bindData(data: NewSoundLayout, isLastItem: Boolean) {
		this.data = data

		this.label.text = data.label
		this.label.isSelected = data.isSelected
		this.selectionIndicator.visibility = if (data.isSelected) View.VISIBLE else View.INVISIBLE

		this.label.isActivated = data.isSelectedForDeletion
		this.itemView.isSelected = data.isSelectedForDeletion

		this.divider.visibility = if (isLastItem) View.INVISIBLE else View.VISIBLE
	}
}