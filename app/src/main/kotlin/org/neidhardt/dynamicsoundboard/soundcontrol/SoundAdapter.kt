package org.neidhardt.dynamicsoundboard.soundcontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnSoundsChangedEventListener
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundAddedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButton
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
public class SoundAdapter
(
		presenter: SoundPresenter,
		soundsDataStorage: SoundsDataStorage,
		eventBus: EventBus

) :
		SoundProgressAdapter<SoundViewHolder>(),
		MediaPlayerEventListener
{
    private var TAG = javaClass.getName();

	private val presenter = presenter
	private val soundsDataStorage = soundsDataStorage
	private val eventBus = eventBus

	override fun onAttachedToWindow()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onDetachedFromWindow() { this.eventBus.unregister(this) }

	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		val playerId = event.getPlayerId()
		val players = this.getValues()
		val count = players.size()
		for (i in 0..count - 1)
		{
			if (players.get(i).getMediaPlayerData().getPlayerId() == playerId)
				this.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		Logger.d(TAG, "onEvent :" + event)
	}

	override fun getValues(): List<EnhancedMediaPlayer>
	{
		return this.presenter.values
	}

	override fun getItemCount(): Int
	{
		return this.getValues().size()
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_sound_item
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder
	{
		val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
		return SoundViewHolder(view, this.eventBus, this.soundsDataStorage, {player, position -> delete(player)}, this)
	}

	private fun delete(player: EnhancedMediaPlayer)
	{
		this.soundsDataStorage.removeSounds(listOf(player))
	}

	override fun onBindViewHolder(holder: SoundViewHolder, position: Int)
	{
		holder.bindData(this.getValues().get(position))
		holder.showShadowForLastItem(position == this.getItemCount() - 1)
		holder.setLabelToDeletionSettings(SoundboardPreferences.isOneSwipeToDeleteEnabled())
	}
}
