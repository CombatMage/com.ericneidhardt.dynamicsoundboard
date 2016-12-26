package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.android_utils.recyclerview_utils.adapter.BaseAdapter
import org.neidhardt.dynamicsoundboard.manager.NewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.utils.longHash

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundAdapter (
		private val itemTouchHelper: ItemTouchHelper,
		private val presenter: SoundPresenter,
		private val playlistManager: NewPlaylistManager,
		private val eventBus: EventBus

) :
		BaseAdapter<MediaPlayerController, SoundViewHolder>()
{
	init { this.setHasStableIds(true) }

	override val values: List<MediaPlayerController> get() = this.presenter.values

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.longHash

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
		return SoundViewHolder(
				itemTouchHelper = this.itemTouchHelper,
				itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_sound_control_item, parent, false),
				eventBus = this.eventBus,
				playlistManager = this.playlistManager)
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
		holder.bindData(this.values[position])
	}
}
