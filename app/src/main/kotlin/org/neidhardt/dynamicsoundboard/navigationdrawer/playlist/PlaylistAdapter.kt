package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistAdapter : BaseAdapter<MediaPlayerController, PlaylistViewHolder>() {

	private val manager = SoundboardApplication.playlistManager

	val clicksViewHolder: PublishSubject<PlaylistViewHolder> = PublishSubject()

	init { this.setHasStableIds(true) }

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.hashCode().toLong()

	override val values: List<MediaPlayerController> get() = this.manager.playlist

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