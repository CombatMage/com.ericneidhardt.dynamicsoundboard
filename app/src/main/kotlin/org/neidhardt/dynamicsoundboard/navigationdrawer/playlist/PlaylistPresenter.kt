package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnPlaylistChangedEventListener
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
public class PlaylistPresenter
(
		private val soundsDataStorage: SoundsDataStorage
) :
		NavigationDrawerListPresenter<Playlist>(),
		NavigationDrawerItemClickListener<EnhancedMediaPlayer>,
		OnPlaylistChangedEventListener,
		MediaPlayerEventListener
{
	public var adapter: PlaylistAdapter? = null

	public val values: MutableList<EnhancedMediaPlayer> = ArrayList()

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

	override fun deleteSelectedItems()
	{
		val playersToRemove = this.getPlayersSelectedForDeletion()

		// TODO
		//this.soundsDataStorage.removeSoundsFromPlaylist(playersToRemove)
		//this.adapter.notifyDataSetChanged()

		super<NavigationDrawerListPresenter>.onSelectedItemsDeleted()
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
			if (player.getMediaPlayerData().getIsSelectedForDeletion())
				selectedItems.add(player)
		}
		return selectedItems
	}

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedPlayers = this.getPlayersSelectedForDeletion()
		for (player in selectedPlayers)
		{
			player.getMediaPlayerData().setIsSelectedForDeletion(false)
			this.adapter?.notifyItemChanged(player)
		}
	}

	override fun onItemClick(data: EnhancedMediaPlayer)
	{
		if (this.isInSelectionMode())
		{
			data.getMediaPlayerData().setIsSelectedForDeletion(!data.getMediaPlayerData().getIsSelectedForDeletion())
			this.adapter?.notifyItemChanged(data)
			super<NavigationDrawerListPresenter>.onItemSelectedForDeletion()
		} else
			this.startOrStopPlayList(data)
	}

	public fun startOrStopPlayList(nextActivePlayer: EnhancedMediaPlayer)
	{
		// TODO

		/*
		val sounds = this.getValues()
		if (!this.getValues().contains(nextActivePlayer))
			throw IllegalStateException("next active player " + nextActivePlayer + " is not in playlist")

		this.currentItemIndex = sounds.indexOf(nextActivePlayer)
		for (player in sounds) {
			if (player == nextActivePlayer)
				continue
			player.stopSound()
		}

		if (nextActivePlayer.isPlaying()) {
			this.stopProgressUpdateTimer()
			nextActivePlayer.pauseSound()
		} else {
			this.startProgressUpdateTimer()
			nextActivePlayer.playSound()
		}
		this.notifyDataSetChanged()*/
	}


	override fun onEventMainThread(event: PlaylistChangedEvent)
	{
		throw UnsupportedOperationException()
	}

	override fun onEvent(event: MediaPlayerStateChangedEvent?)
	{
		throw UnsupportedOperationException()
	}

	override fun onEvent(event: MediaPlayerCompletedEvent?)
	{
		throw UnsupportedOperationException()
	}
}