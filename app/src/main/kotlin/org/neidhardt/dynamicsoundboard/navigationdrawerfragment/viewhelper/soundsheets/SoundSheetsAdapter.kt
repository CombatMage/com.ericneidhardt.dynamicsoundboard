package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.subjects.PublishSubject
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.model.SoundSheet

open class SoundSheetsAdapter : BaseAdapter<SoundSheet, SoundSheetViewHolder>() {

	val clicksViewHolder: PublishSubject<SoundSheetViewHolder> = PublishSubject.create()

	init { this.setHasStableIds(true) }

	fun setValues(soundSheets: List<SoundSheet>) {
		this.values.clear()
		this.values.addAll(soundSheets)
	}

	override fun getItemId(position: Int): Long = this.values[position].fragmentTag.hashCode().toLong()

	override val values: MutableList<SoundSheet> = ArrayList()

	override fun getItemViewType(position: Int): Int = R.layout.view_sound_sheet_item

	override fun getItemCount(): Int = this.values.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundSheetViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		val viewHolder = SoundSheetViewHolder(view)
		RxView.clicks(view)
				.takeUntil(RxView.detaches(parent))
				.map { viewHolder }
				.subscribe(this.clicksViewHolder)

		return viewHolder
	}

	override fun onBindViewHolder(holder: SoundSheetViewHolder, position: Int) {
		val data = this.values[position]

		holder.bindData(data, position == this.itemCount - 1)
	}
}
