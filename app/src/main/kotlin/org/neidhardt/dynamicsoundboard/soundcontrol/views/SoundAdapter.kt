package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.android_utils.views.RxCustomEditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.utils.longHash
import rx.subjects.PublishSubject

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundAdapter (
		private val presenter: SoundPresenter,
		private val itemTouchHelper: ItemTouchHelper,
		private val playlistManager: PlaylistManager
) :
		BaseAdapter<MediaPlayerController, SoundViewHolder>()
{
	init { this.setHasStableIds(true) }

	val clicksTogglePlaylist: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val clicksSettings: PublishSubject<SoundViewHolder> = PublishSubject.create()
	val changesName: PublishSubject<SoundViewHolder> = PublishSubject.create()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
		val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_sound_control_item, parent, false)
		val viewHolder = SoundViewHolder(
				itemTouchHelper = this.itemTouchHelper,
				itemView = itemView,
				playlistManager = this.playlistManager)

		val parentIsDetached = RxView.detaches(parent)

		RxView.clicks(viewHolder.inPlaylistButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksTogglePlaylist.onNext(it) }

		RxView.clicks(viewHolder.settingsButton)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.clicksSettings.onNext(it) }

		RxCustomEditText.editsText(viewHolder.name)
				.takeUntil(parentIsDetached)
				.map { viewHolder }
				.subscribe { this.changesName.onNext(it) }

		return viewHolder
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int) { holder.bindData(this.values[position]) }

	override val values: List<MediaPlayerController> get() = this.presenter.values

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.longHash
}
