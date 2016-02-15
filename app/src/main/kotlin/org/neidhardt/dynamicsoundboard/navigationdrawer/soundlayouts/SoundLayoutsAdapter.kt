package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.ListAdapter

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
class SoundLayoutsAdapter
(
		private val presenter: SoundLayoutsPresenter,
		private val eventBus: EventBus
) :
		RecyclerView.Adapter<SoundLayoutViewHolder>(),
		ListAdapter<SoundLayout>,
		NavigationDrawerItemClickListener<SoundLayout>
{
	fun getValues(): List<SoundLayout>
	{
		return this.presenter.values
	}

	override fun getItemCount(): Int
	{
		return this.getValues().size
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_sound_layout_item
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundLayoutViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		return SoundLayoutViewHolder(view, this, object: NavigationDrawerItemClickListener<SoundLayout>
		{
			override fun onItemClick(data: SoundLayout)
			{
				eventBus.post(OpenSoundLayoutSettingsEvent(data))
			}
		})
	}

	override fun onBindViewHolder(holder: SoundLayoutViewHolder, position: Int)
	{
		val data = this.getValues()[position]
		holder.bindData(data)
	}

	override fun notifyItemChanged(data: SoundLayout)
	{
		val index = this.getValues().indexOf(data)
		if (index == -1)
			this.notifyDataSetChanged()
		else
			this.notifyItemChanged(index)
	}

	override fun onItemClick(data: SoundLayout)
	{
		this.presenter.onItemClick(data)
	}
}
