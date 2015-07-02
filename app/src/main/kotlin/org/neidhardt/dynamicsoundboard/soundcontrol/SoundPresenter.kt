package org.neidhardt.dynamicsoundboard.soundcontrol

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
public class SoundPresenter
(
		fragmentTag: String,
		eventBus: EventBus,
		soundsDataAccess: SoundsDataAccess
) : OnSoundsChangedEventListener
{
	private val eventBus = eventBus
	private val fragmentTag = fragmentTag
	private val soundsDataAccess = soundsDataAccess

	public var adapter: SoundAdapter? = null

	public val values: MutableList<EnhancedMediaPlayer> = ArrayList<EnhancedMediaPlayer>()

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

	override fun onEventMainThread(event: SoundAddedEvent)
	{
		val newPlayer = event.getPlayer()
		if (newPlayer.getMediaPlayerData().getFragmentTag().equals(this.fragmentTag))
		{
			val count = this.values.size()
			val positionToInsert = newPlayer.getMediaPlayerData().getSortOrder()
			if (positionToInsert == null)
			{
				newPlayer.getMediaPlayerData().setSortOrder(count)
				this.insertPlayerAndUpdateSortOrder(count, newPlayer) // append to end of list
			}
			else
			{
				for (i in 0..count - 1)
				{
					val existingPlayer = this.values.get(i)
					if (positionToInsert < existingPlayer.getMediaPlayerData().getSortOrder())
					{
						this.insertPlayerAndUpdateSortOrder(i, newPlayer)
						return
					}
				}
				this.insertPlayerAndUpdateSortOrder(count, newPlayer) // append to end of list
			}
		}
	}

	private fun insertPlayerAndUpdateSortOrder(position: Int, player: EnhancedMediaPlayer)
	{
		this.values.add(position, player)
		this.adapter?.notifyItemInserted(position)
		if (position == this.values.size() - 1)
			this.adapter?.notifyItemChanged(position - 1)

		this.updateSortOrdersAfter(position, true)
	}

	override fun onEventMainThread(event: SoundMovedEvent)
	{
		val movedPlayer = event.getPlayer()
		if (movedPlayer.getMediaPlayerData().getFragmentTag().equals(this.fragmentTag))
		{
			this.values.remove(event.getFrom())
			this.values.add(event.getTo(), movedPlayer)

			val start = Math.min(event.getFrom(), event.getTo()); // we need to update all sound after the moved one
			val end = Math.max(event.getFrom(), event.getTo());

			for (i in start..end)
			{
				val playerData = this.values.get(i).getMediaPlayerData()
				playerData.setSortOrder(i);
				playerData.updateItemInDatabaseAsync();
			}

			this.adapter?.notifyDataSetChanged()
		}
	}

	private fun updateSortOrdersAfter(index: Int, itemInserted: Boolean)
	{
		val count = this.values.size();
		for (i in index..count - 1)
		{
			val playerData = this.values.get(i).getMediaPlayerData()
			val sortOrder = playerData.getSortOrder()
			playerData.setSortOrder(if (itemInserted) sortOrder + 1 else sortOrder - 1);
			playerData.updateItemInDatabaseAsync();
		}
	}

	override fun onEventMainThread(event: SoundsRemovedEvent)
	{
		val players = event.getPlayers()
		if (players == null)
			this.adapter?.notifyDataSetChanged()
		else
		{
			for (player in players)
				this.removePlayerAndUpdateSortOrder(player)
		}
	}

	private fun removePlayerAndUpdateSortOrder(player: EnhancedMediaPlayer)
	{
		val index = this.values.indexOf(player)
		if (index != -1)
		{
			this.values.remove(player)
			this.adapter?.notifyItemRemoved(index)

			this.updateSortOrdersAfter(index, false)
		}
	}

	override fun onEventMainThread(event: SoundChangedEvent)
	{
		val player = event.getPlayer()
		val index = this.values.indexOf(player)
		if (index != -1)
			this.adapter?.notifyItemChanged(index)
	}

}