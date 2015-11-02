package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
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
		private val soundsDataStorage: SoundsDataStorage,
		private val soundsDataAccess: SoundsDataAccess

) :
		NavigationDrawerListPresenter<Playlist>(),
		NavigationDrawerItemClickListener<EnhancedMediaPlayer>,
		OnPlaylistChangedEventListener,
		MediaPlayerEventListener
{
	public var adapter: PlaylistAdapter? = null

	public val values: MutableList<EnhancedMediaPlayer> = ArrayList()

	private var currentItemIndex: Int? = null

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.values.clear()

		val playlist = this.soundsDataAccess.getPlaylist()
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

	override fun getNumberOfItemsSelectedForDeletion(): Int
	{
		return this.getPlayersSelectedForDeletion().size()
	}

	private fun getPlayersSelectedForDeletion(): List<EnhancedMediaPlayer>
	{
		val selectedItems = ArrayList<EnhancedMediaPlayer>()
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

	override fun onItemClick(data: EnhancedMediaPlayer)
	{
		if (this.isInSelectionMode)
		{
			data.mediaPlayerData.isSelectedForDeletion = !data.mediaPlayerData.isSelectedForDeletion
			this.adapter?.notifyItemChanged(data)
			super.onItemSelectedForDeletion()
		} else
			this.startOrStopPlayList(data)
	}

	public fun startOrStopPlayList(nextActivePlayer: EnhancedMediaPlayer)
	{
		if (!this.values.contains(nextActivePlayer))
			throw IllegalStateException("next active player $nextActivePlayer is not in playlist")

		this.currentItemIndex = this.values.indexOf(nextActivePlayer)
		for (player in this.values)
		{
			if (player != nextActivePlayer)
				player.stopSound()
		}

		if (nextActivePlayer.isPlaying)
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

		val playlist = this.soundsDataAccess.getPlaylist()
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
			val currentPlayer = this.values.get(this.currentItemIndex!!).mediaPlayerData
			if (currentPlayer !== finishedPlayerData)
				return

			this.currentItemIndex = (this.currentItemIndex as Int) + 1
			if ((this.currentItemIndex as Int) >= this.values.size())
				this.currentItemIndex = 0

			this.values.get(this.currentItemIndex!!).playSound()
			this.adapter?.notifyDataSetChanged()
		}
	}

	private fun setPlaylistSortOrder(playlist: List<EnhancedMediaPlayer>)
	{
		val count = playlist.size()
		for (i in 0..count - 1)
		{
			playlist.get(i).getMediaPlayerData()?.sortOrder = i
			playlist.get(i).mediaPlayerData?.updateItemInDatabaseAsync()
		}
	}
}