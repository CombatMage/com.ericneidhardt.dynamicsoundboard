package org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.subjects.PublishSubject
import org.neidhardt.androidutils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.app_utils.longHash
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.manager.containsPlayerWithId
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
data class SoundViewHolderEvent<out T>(val viewHolder: SoundViewHolder, val data: T)

class SoundAdapter : BaseAdapter<MediaPlayerController, SoundViewHolder>() {

	init { this.setHasStableIds(true) }

	val startsReorder: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val startsSwipe: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val clicksPlay: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksStop: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val clicksTogglePlaylist: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksSettings: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksLoopEnabled: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksName: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val seeksToPosition: PublishSubject<SoundViewHolderEvent<Int>> = PublishSubject.create()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
		val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_sound_control_item, parent, false)
		val viewHolder = SoundViewHolder(itemView)

		val parentIsDetached = RxView.detaches(parent)

		RxView.touches(viewHolder.reorder)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.startsReorder.onNext(it) }

		RxView.touches(itemView)
				.takeUntil(parentIsDetached)
				.filter { _ ->
					// only start swipe if recyclerView is not scrolling (or else busy)
					(parent as RecyclerView).scrollState == RecyclerView.SCROLL_STATE_IDLE
				}
				.map { viewHolder }
				.subscribe {
					this.startsSwipe.onNext(it)
				}

		RxView.clicks(viewHolder.playButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksPlay.onNext(it) }

		RxView.clicks(viewHolder.stopButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksStop.onNext(it) }

		RxView.clicks(viewHolder.inPlaylistButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksTogglePlaylist.onNext(it) }

		RxView.clicks(viewHolder.settingsButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksSettings.onNext(it) }

		RxView.clicks(viewHolder.isLoopEnabledButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksLoopEnabled.onNext(it) }

		RxView.clicks(viewHolder.name)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksName.onNext(it) }

		RxSeekBar.userChanges(viewHolder.timePosition)
				.takeUntil(parentIsDetached)
				.skip(1)
				.map { int -> SoundViewHolderEvent(viewHolder, int) }
				.subscribe { this.seeksToPosition.onNext(it) }

		return viewHolder
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
		val playerData = this.values[position]
		val isPlayerInPlaylist = this.currentPlaylist.containsPlayerWithId(
				playerData.mediaPlayerData.playerId
		)
		holder.bindData(playerData, isPlayerInPlaylist)
	}

	var currentPlaylist: List<MediaPlayerController> = ArrayList()

	override var values: List<MediaPlayerController> = ArrayList()

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.longHash
}
