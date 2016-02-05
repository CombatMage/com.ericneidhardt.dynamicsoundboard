package org.neidhardt.dynamicsoundboard.soundcontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
public class SoundAdapter
(
		private val presenter: SoundPresenter,
		private val soundsDataStorage: SoundsDataStorage,
		private val eventBus: EventBus

) :
		SoundProgressAdapter<SoundViewHolder>()
{
	override fun getValues(): List<MediaPlayerController> = this.presenter.values

	override fun getItemCount(): Int = this.getValues().size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder =
			SoundViewHolder(
				LayoutInflater.from(parent.context).inflate(R.layout.view_sound_item, parent, false),
				this.eventBus,
				this.soundsDataStorage,
				{player, position -> delete(player)},
				this)

	private fun delete(player: MediaPlayerController)
	{
		this.soundsDataStorage.removeSounds(listOf(player))
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int)
	{
		holder.bindData(this.getValues()[position])
		holder.showShadowForLastItem(position == this.itemCount - 1)
		holder.setLabelToDeletionSettings(SoundboardPreferences.isOneSwipeToDeleteEnabled)
	}
}
