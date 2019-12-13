package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.subjects.PublishSubject
import org.neidhardt.androidutils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistAdapter : BaseAdapter<MediaPlayerController, PlaylistViewHolder>() {

	val clicksViewHolder: PublishSubject<PlaylistViewHolder> = PublishSubject.create()

	init { this.setHasStableIds(true) }

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.hashCode().toLong()

	override var values: List<MediaPlayerController> = ArrayList()

	override fun getItemViewType(position: Int): Int = R.layout.view_playlist_item

	override fun getItemCount(): Int = this.values.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		val viewHolder = PlaylistViewHolder(view)

		RxView.clicks(view)
				.takeUntil(RxView.detaches(parent))
				.map { viewHolder }
				.subscribe(this.clicksViewHolder)

		return viewHolder
	}

	override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
		val data = this.values[position]
		holder.bindData(data, position == this.itemCount - 1)
	}
}