package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetsChangedEventListener
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetAddedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetChangedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsRemovedEvent
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.BaseAdapter
import java.util.HashSet

public class SoundSheetsAdapter
(
		private val presenter: SoundSheetsPresenter,
 		private val eventBus: EventBus
) :
		BaseAdapter<SoundSheet, SoundSheetViewHolder>(),
		OnItemClickListener
{
	override fun onAttachedToWindow()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
		this.notifyDataSetChanged()
	}

	override fun onDetachedFromWindow() { this.eventBus.unregister(this) }

	override fun getValues(): List<SoundSheet>
	{
		return this.presenter.values
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_sound_sheet_item
	}

	override fun getItemCount(): Int {
		return this.getValues().size()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
	{
		val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
		return SoundSheetViewHolder(view, this)
	}

	override fun onBindViewHolder(holder: SoundSheetViewHolder, position: Int)
	{
		val data = this.getValues().get(position)

		val sounds = this.presenter.getSoundsInFragment(data.getFragmentTag())
		val soundCount = sounds.size()

		holder.bindData(data, soundCount)
	}

	override fun onItemClick(data: SoundSheet)
	{
		this.presenter.onItemClick()
	}
}

public interface OnItemClickListener
{
	public fun onItemClick(data: SoundSheet)
}