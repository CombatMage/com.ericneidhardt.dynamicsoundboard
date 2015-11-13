package org.neidhardt.dynamicsoundboard.soundcontrol

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import java.util.*

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
public class SoundPresenter
(
		private val fragmentTag: String,
		private val eventBus: EventBus,
		private val soundsDataAccess: SoundsDataAccess
) :
		OnSoundsChangedEventListener,
		MediaPlayerEventListener
{
	private val TAG = javaClass.name

	public var adapter: SoundAdapter? = null

	public val values: MutableList<MediaPlayerController> = ArrayList()

	fun onAttachedToWindow()
	{
		this.values.clear()
		this.values.addAll(this.soundsDataAccess.getSoundsInFragment(this.fragmentTag))
		this.adapter!!.notifyDataSetChanged()

		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	fun onDetachedFromWindow()
	{
		this.eventBus.unregister(this)
	}

	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		val playerId = event.playerId
		val players = this.values
		val count = players.size
		for (i in 0..count - 1)
		{
			if (players.get(i).mediaPlayerData.playerId == playerId)
				this.adapter?.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		Logger.d(TAG, "onEvent :" + event)
	}

	override fun onEventMainThread(event: SoundAddedEvent)
	{
		val newPlayer = event.player
		if (newPlayer.mediaPlayerData.fragmentTag.equals(this.fragmentTag))
		{
			val count = this.values.size
			val positionToInsert = newPlayer.mediaPlayerData.sortOrder
			if (positionToInsert == null)
			{
				newPlayer.mediaPlayerData.sortOrder = count
				newPlayer.mediaPlayerData.updateItemInDatabaseAsync()
				this.insertPlayer(count, newPlayer) // append to end of list
			}
			else
			{
				for (i in 0..count - 1)
				{
					val existingPlayer = this.values.get(i)
					if (positionToInsert < existingPlayer.mediaPlayerData.sortOrder)
					{
						this.insertPlayer(i, newPlayer)
						return
					}
				}
				this.insertPlayer(count, newPlayer) // append to end of list
			}
		}
	}

	private fun insertPlayer(position: Int, player: MediaPlayerController)
	{
		this.values.add(position, player)
		this.adapter?.notifyItemInserted(position)
		if (position == this.values.size - 1)
			this.adapter?.notifyItemChanged(position - 1)
	}

	override fun onEventMainThread(event: SoundMovedEvent)
	{
		val movedPlayer = event.player
		if (movedPlayer.mediaPlayerData.fragmentTag.equals(this.fragmentTag))
		{
			this.values.removeAt(event.from)
			this.values.add(event.to, movedPlayer)

			val start = Math.min(event.from, event.to); // we need to update all sound after the moved one
			val end = Math.max(event.from, event.to);

			for (i in start..end)
			{
				val playerData = this.values.get(i).mediaPlayerData
				playerData.sortOrder = i;
				playerData.updateItemInDatabaseAsync();
			}

			this.adapter?.notifyDataSetChanged()
		}
	}

	override fun onEventMainThread(event: SoundsRemovedEvent)
	{
		if (event.removeAll())
			this.adapter?.notifyDataSetChanged()
		else
		{
			val players = event.players
			for (player in players.orEmpty())
				this.removePlayerAndUpdateSortOrder(player)
		}
	}

	private fun removePlayerAndUpdateSortOrder(player: MediaPlayerController)
	{
		val index = this.values.indexOf(player)
		if (index != -1)
		{
			this.values.remove(player)
			this.adapter?.notifyItemRemoved(index)

			this.updateSortOrdersAfter(index - 1) // -1 to ensure item at index (which was index + 1 before) is also updated
		}
	}

	private fun updateSortOrdersAfter(index: Int)
	{
		val count = this.values.size
		for (i in index + 1 .. count - 1)
		{
			val playerData = this.values.get(i).mediaPlayerData
			val sortOrder = playerData.sortOrder
			playerData.sortOrder = sortOrder - 1;
			playerData.updateItemInDatabaseAsync();
		}
	}

	override fun onEventMainThread(event: SoundChangedEvent)
	{
		val player = event.player
		val index = this.values.indexOf(player)
		if (index != -1)
			this.adapter?.notifyItemChanged(index)
	}

}