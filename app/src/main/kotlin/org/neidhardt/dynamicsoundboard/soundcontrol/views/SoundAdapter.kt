package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxSeekBar
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.android_utils.views.RxCustomEditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.utils.longHash
import rx.subjects.PublishSubject

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
data class SoundViewHolderEvent<out T>(val viewHolder: SoundViewHolder, val data: T)

class SoundAdapter (
		private val soundSheet: NewSoundSheet,
		private val soundManager: SoundManager,
		private val playlistManager: PlaylistManager
) :
		BaseAdapter<MediaPlayerController, SoundViewHolder>()
{
	init { this.setHasStableIds(true) }

	val startsReorder: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val startsSwipe: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val clicksPlay: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksStop: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val clicksTogglePlaylist: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksSettings: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksLoopEnabled: PublishSubject<SoundViewHolder> = PublishSubject.create()

	val changesName: PublishSubject<SoundViewHolderEvent<String>> = PublishSubject.create()
	val seeksToPosition: PublishSubject<SoundViewHolderEvent<Int>> = PublishSubject.create()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
		val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_sound_control_item, parent, false)
		val viewHolder = SoundViewHolder(
				itemView = itemView,
				playlistManager = this.playlistManager)

		val parentIsDetached = RxView.detaches(parent)

		RxView.touches(viewHolder.reorder)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.startsReorder.onNext(it) }

		RxView.touches(itemView)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.startsSwipe.onNext(it) }

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

		RxCustomEditText.editsText(viewHolder.name)
				.takeUntil(parentIsDetached)
				.map { string -> SoundViewHolderEvent(viewHolder, string) }
				.subscribe { this.changesName.onNext(it) }

		RxSeekBar.userChanges(viewHolder.timePosition)
				.takeUntil(parentIsDetached)
				.map { int -> SoundViewHolderEvent(viewHolder, int) }
				.skip(1)
				.subscribe { this.seeksToPosition.onNext(it) }

		return viewHolder
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int) { holder.bindData(this.values[position]) }

	override val values: List<MediaPlayerController> get() {
		val players = this.soundManager.sounds.getOrElse(this.soundSheet, { emptyList() } )
		return players
	}

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.longHash
}
