package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.view.LayoutInflater
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.longHash
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundAdapter
(
		private val presenter: SoundPresenter,
		private val soundsDataStorage: SoundsDataStorage,
		private val eventBus: EventBus

) :
		SoundProgressAdapter<SoundViewHolder>()
{
	init { this.setHasStableIds(false) }

	override val values: List<MediaPlayerController>
		get() = this.presenter.values

	override fun getItemCount(): Int = this.values.size

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.longHash

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder =
			SoundViewHolder(
					itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_sound_control, parent, false),
					eventBus = this.eventBus,
					soundsDataStorage = this.soundsDataStorage,
					progressTimer = this)

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int)
	{
		holder.bindData(this.values[position])
	}
}
