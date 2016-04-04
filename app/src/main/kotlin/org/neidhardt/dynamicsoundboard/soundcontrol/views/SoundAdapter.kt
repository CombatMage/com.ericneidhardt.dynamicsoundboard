package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.view.LayoutInflater
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
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
	override val values: List<MediaPlayerController>
		get() = this.presenter.values

	override fun getItemCount(): Int = this.values.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder =
			SoundViewHolder(
					LayoutInflater.from(parent.context).inflate(R.layout.view_sound_item, parent, false),
					this.eventBus,
					this.soundsDataStorage,
					this)

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int)
	{
		holder.bindData(this.values[position])
		holder.setLabelToDeletionSettings(SoundboardPreferences.isOneSwipeToDeleteEnabled)
	}
}
