package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnPlaylistChangedEventListener
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.util.*

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
fun createPlaylistPresenter(
		eventBus: EventBus, recyclerView: RecyclerView, soundsDataAccess: SoundsDataAccess, soundsDataStorage: SoundsDataStorage): PlaylistPresenter
{
	return PlaylistPresenter(
			eventBus = eventBus,
			soundsDataAccess = soundsDataAccess,
			soundsDataStorage = soundsDataStorage
	).apply {
		this.adapter = PlaylistAdapter(this)
		this.adapter?.recyclerView = recyclerView
		this.view = recyclerView
	}
}

class PlaylistPresenter
(
		override val eventBus: EventBus,
		val soundsDataStorage: SoundsDataStorage,
		private val soundsDataAccess: SoundsDataAccess

) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<MediaPlayerController>,
		OnPlaylistChangedEventListener,
		MediaPlayerEventListener
{
	override var view: RecyclerView? = null

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
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getPlayersSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

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

	override fun selectAllItems()
	{
		val selectedPlayers = this.values
		for (player in selectedPlayers)
		{
			player.mediaPlayerData.isSelectedForDeletion = true
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

	fun startOrStopPlayList(nextActivePlayer: MediaPlayerController)
	{
		if (!this.values.contains(nextActivePlayer))
			throw IllegalStateException("next active player $nextActivePlayer is not in playlist")

		this.currentItemIndex = this.values.indexOf(nextActivePlayer)
		for (player in this.values)
		{
			if (player != nextActivePlayer && player.isPlayingSound)
				player.stopSound()
		}

		if (nextActivePlayer.isPlayingSound)
		{
			this.adapter?.stopProgressUpdateTimer()
			nextActivePlayer.stopSound()
		} else
		{
			this.adapter?.startProgressUpdateTimer()
			nextActivePlayer.playSound()
		}
		this.adapter?.notifyDataSetChanged()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: PlaylistChangedEvent)
	{
		this.values.clear()

		val playlist = this.soundsDataAccess.playlist
		this.setPlaylistSortOrder(playlist)
		this.values.addAll(playlist)

		this.adapter?.notifyDataSetChanged()
		this.adapter?.startProgressUpdateTimer()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
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

	@Subscribe(threadMode = ThreadMode.MAIN)
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
