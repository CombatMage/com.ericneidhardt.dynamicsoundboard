package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.BaseAdapter

public open class SoundSheetsAdapter
(
		private val presenter: SoundSheetsPresenter
) :
		BaseAdapter<SoundSheet, SoundSheetViewHolder>(),
		NavigationDrawerItemClickListener<SoundSheet>
{
	override fun getValues(): List<SoundSheet>
	{
		return this.presenter.values
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_sound_sheet_item
	}

	override fun getItemCount(): Int
	{
		return this.getValues().size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundSheetViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		return SoundSheetViewHolder(view, this)
	}

	override fun onBindViewHolder(holder: SoundSheetViewHolder, position: Int)
	{
		val data = this.getValues().get(position)

		val sounds = this.presenter.getSoundsInFragment(data.fragmentTag)
		val soundCount = sounds.size

		holder.bindData(data, soundCount)
	}

	override fun onItemClick(data: SoundSheet)
	{
		this.presenter.onItemClick(data)
	}
}
