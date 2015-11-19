package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnPlaylistChangedEventListener
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.util.*

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
public class PlaylistPresenter
(
		override val eventBus: EventBus,
		val soundsDataStorage: SoundsDataStorage,
		private val soundsDataAccess: SoundsDataAccess

) :
		NavigationDrawerListPresenter<Playlist?>(),
		NavigationDrawerItemClickListener<MediaPlayerController>,
		OnPlaylistChangedEventListener,
		MediaPlayerEventListener
{
	override val isEventBusSubscriber: Boolean = true
	override var view: Playlist? = null

	var adapter: PlaylistAdapter? = null
	val values: MutableList<MediaPlayerController> = ArrayList()

	private var currentItemIndex: Int? = null

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.values.clear()

		val playlist = this.soundsDataAccess.playlist
		this.setPlaylistSortOrder(playlist)
		this.values.addAll(playlist)

		this.adapter?.notifyDataSetChanged()
		this.adapter?.startProgressUpdateTimer()
	}

	override fun onDetachedFromWindow()
	{
		this.adapter?.stopProgressUpdateTimer()
	}

	override fun deleteSelectedItems()
	{
		val playersToRemove = this.getPlayersSelectedForDeletion()

		for (player in playersToRemove)
		{
			val index = this.values.indexOf(player)
			this.values.remove(player)
			this.adapter?.notifyItemRemoved(index)

		}
		this.soundsDataStorage.removeSoundsFromPlaylist(playersToRemove)

		super.onSelectedItemsDeleted()
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getPlayersSelectedForDeletion().size

	private fun getPlayersSelectedForDeletion(): List<MediaPlayerController>
	{
		val selectedItems = ArrayList<MediaPlayerController>()
		val existingItems = this.values
		for (player in existingItems) {
			if (player.mediaPlayerData.isSelectedForDeletion)
				selectedItems.add(player)
		}
		return selectedItems
	}

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedPlayers = this.getPlayersSelectedForDeletion()
		for (player in selectedPlayers)
		{
			player.mediaPlayerData.isSelectedForDeletion = false
			this.adapter?.notifyItemChanged(player)
		}
	}

	override fun onItemClick(data: MediaPlayerController)
	{
		if (this.isInSelectionMode)
		{
			data.mediaPlayerData.isSelectedForDeletion = !data.mediaPlayerData.isSelectedForDeletion
			this.adapter?.notifyItemChanged(data)
			super.onItemSelectedForDeletion()
		} else
			this.startOrStopPlayList(data)
	}

	public fun startOrStopPlayList(nextActivePlayer: MediaPlayerController)
	{
		if (!this.values.contains(nextActivePlayer))
			throw IllegalStateException("next active player $nextActivePlayer is not in playlist")

		this.currentItemIndex = this.values.indexOf(nextActivePlayer)
		for (player in this.values)
		{
			if (player != nextActivePlayer)
				player.stopSound()
		}

		if (nextActivePlayer.isPlayingSound)
		{
			this.adapter?.stopProgressUpdateTimer()
			nextActivePlayer.pauseSound()
		} else
		{
			this.adapter?.startProgressUpdateTimer()
			nextActivePlayer.playSound()
		}
		this.adapter?.notifyDataSetChanged()
	}

	override fun onEventMainThread(event: PlaylistChangedEvent)
	{
		this.values.clear()

		val playlist = this.soundsDataAccess.playlist
		this.setPlaylistSortOrder(playlist)
		this.values.addAll(playlist)

		this.adapter?.notifyDataSetChanged()
		this.adapter?.startProgressUpdateTimer()
	}

	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		val player = event.player
		if (this.values.contains(player) && !event.isAlive)
		{
			val index = this.values.indexOf(player)
			this.values.remove(player)

			this.setPlaylistSortOrder(this.values)

			this.adapter?.notifyItemRemoved(index)
		}
		else
			this.adapter?.notifyDataSetChanged()
	}

	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		val finishedPlayerData = event.player.mediaPlayerData
		if (this.currentItemIndex != null)
		{
			val currentPlayer = this.values[this.currentItemIndex!!].mediaPlayerData
			if (currentPlayer !== finishedPlayerData)
				return

			this.currentItemIndex = (this.currentItemIndex as Int) + 1
			if ((this.currentItemIndex as Int) >= this.values.size)
				this.currentItemIndex = 0

			this.values[this.currentItemIndex!!].playSound()
			this.adapter?.notifyDataSetChanged()
		}
	}

	private fun setPlaylistSortOrder(playlist: List<MediaPlayerController>)
	{
		val count = playlist.size
		for (i in 0..count - 1)
		{
			playlist[i].mediaPlayerData.sortOrder = i
			playlist[i].mediaPlayerData.updateItemInDatabaseAsync()
		}
	}
}