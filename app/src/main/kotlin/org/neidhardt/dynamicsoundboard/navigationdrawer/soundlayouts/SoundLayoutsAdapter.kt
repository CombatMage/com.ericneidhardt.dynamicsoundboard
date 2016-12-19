package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.view.LayoutInflater
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.android_utils.recyclerview_utils.adapter.ListAdapter
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.utils.longHash

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
class SoundLayoutsAdapter
(
		private val eventBus: EventBus,
		private val presenter: SoundLayoutsPresenter
) :
		BaseAdapter<NewSoundLayout, SoundLayoutViewHolder>(),
		ListAdapter<NewSoundLayout>,
		NavigationDrawerItemClickListener<NewSoundLayout>
{
	init { this.setHasStableIds(true) }

	override fun getItemId(position: Int): Long = this.values[position].databaseId.longHash

	override val values: List<NewSoundLayout> get() = this.presenter.values

	override fun getItemCount(): Int = this.values.size

	override fun getItemViewType(position: Int): Int = R.layout.view_sound_layout_item

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundLayoutViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		return SoundLayoutViewHolder(view, this, object: NavigationDrawerItemClickListener<NewSoundLayout>
		{
			override fun onItemClick(data: NewSoundLayout)
			{
				eventBus.post(OpenSoundLayoutSettingsEvent(data))
			}
		})
	}

	override fun onBindViewHolder(holder: SoundLayoutViewHolder, position: Int)
	{
		val data = this.values[position]
		holder.bindData(data, position == this.itemCount - 1)
	}

	override fun notifyItemChanged(data: NewSoundLayout)
	{
		val index = this.values.indexOf(data)
		if (index == -1)
			this.notifyDataSetChanged()
		else
			this.notifyItemChanged(index)
	}

	override fun onItemClick(data: NewSoundLayout)
	{
		this.presenter.onItemClick(data)
	}
}
