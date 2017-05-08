package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.subjects.PublishSubject
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.android_utils.recyclerview_utils.adapter.ListAdapter
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.persistance.model.SoundLayout
import org.neidhardt.utils.longHash

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
class SoundLayoutsAdapter() :
		BaseAdapter<SoundLayout, SoundLayoutViewHolder>(),
		ListAdapter<SoundLayout>
{
	val clicksViewHolder: PublishSubject<SoundLayoutViewHolder> = PublishSubject.create()
	val clicksSettings: PublishSubject<SoundLayoutViewHolder> = PublishSubject.create()

	init { this.setHasStableIds(true) }

	private val manager = SoundboardApplication.soundLayoutManager

	override fun getItemId(position: Int): Long = this.values[position].databaseId?.longHash
			?: throw IllegalStateException("SoundLayoutManager has invalid item ${this.values[position]}")

	override val values: List<SoundLayout> get() = this.manager.soundLayouts

	override fun getItemCount(): Int = this.values.size

	override fun getItemViewType(position: Int): Int = R.layout.view_sound_layout_item

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundLayoutViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		val viewHolder = SoundLayoutViewHolder(view)

		val parentDetaches = RxView.detaches(parent)

		RxView.clicks(view)
				.takeUntil(parentDetaches)
				.map { viewHolder }
				.subscribe(this.clicksViewHolder)

		RxView.clicks(viewHolder.openSettings)
				.takeUntil(parentDetaches)
				.map { viewHolder }
				.subscribe(this.clicksSettings)

		return viewHolder
	}

	override fun onBindViewHolder(holder: SoundLayoutViewHolder, position: Int)
	{
		val data = this.values[position]
		holder.bindData(data, position == this.itemCount - 1)
	}

	override fun notifyItemChanged(data: SoundLayout)
	{
		val index = this.values.indexOf(data)
		if (index == -1)
			this.notifyDataSetChanged()
		else
			this.notifyItemChanged(index)
	}
}
