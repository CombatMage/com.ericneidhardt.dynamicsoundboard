package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.eventbus_utils.registerIfRequired
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
private val INDEX_NOT_SET = -1

class PlaylistPresenter(override val eventBus: EventBus) :
		NavigationDrawerListBasePresenter(),
		NavigationDrawerItemClickListener<MediaPlayerController>,
		MediaPlayerEventListener
{
	companion object {
		fun createPlaylistPresenter(eventBus: EventBus, adapter: PlaylistAdapter): PlaylistPresenter {
			return PlaylistPresenter(eventBus).apply {
				this.adapter = adapter
			}
		}
	}

	private val manager = SoundboardApplication.playlistManager

	private var subscriptions = CompositeSubscription()
	var adapter: PlaylistAdapter by Delegates.notNull<PlaylistAdapter>()
	val values: List<MediaPlayerController> get() = this.manager.playlist

	private var currentItemIndex: Int = INDEX_NOT_SET

	override fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)

		this.adapter.notifyDataSetChanged()
		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(RxNewPlaylistManager.playlistChanges(this.manager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter.notifyDataSetChanged() })

		this.subscriptions.add(this.adapter.clicksViewHolder
				.subscribe { viewHolder ->
					viewHolder.player?.let{ this.onItemClick(it) }
				})
	}

	override fun onDetachedFromWindow() {
		this.eventBus.unregister(this)
		this.subscriptions.unsubscribe()
	}

	override fun deleteSelectedItems() {
		val playersToRemove = this.getPlayersSelectedForDeletion()
		this.manager.remove(playersToRemove)
		this.stopDeletionMode()
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getPlayersSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	private fun getPlayersSelectedForDeletion(): List<MediaPlayerController> {
		val existingItems = this.values
		val selectedItems = existingItems.filter { it.mediaPlayerData.isSelectedForDeletion }
		return selectedItems
	}

	override fun deselectAllItemsSelectedForDeletion() {
		val selectedPlayers = this.getPlayersSelectedForDeletion()
		for (player in selectedPlayers)
			player.mediaPlayerData.isSelectedForDeletion = false

		this.adapter.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedPlayers = this.values
		for (player in selectedPlayers) {
			player.mediaPlayerData.isSelectedForDeletion = true
		}
		this.adapter.notifyDataSetChanged()
	}

	override fun onItemClick(data: MediaPlayerController) {
		if (this.isInSelectionMode) {
			data.mediaPlayerData.isSelectedForDeletion = !data.mediaPlayerData.isSelectedForDeletion
			this.adapter.notifyItemChanged(data)
			super.onItemSelectedForDeletion()
		} else
			this.startOrStopPlayList(data)
	}

	fun startOrStopPlayList(nextActivePlayer: MediaPlayerController) {
		if (!this.values.contains(nextActivePlayer))
			throw IllegalStateException("next active player $nextActivePlayer is not in playlist")

		// stop all playing sounds, except the next player
		this.currentItemIndex = this.values.indexOf(nextActivePlayer)
		this.values.filter { it != nextActivePlayer && it.isPlayingSound }
				   .forEach { it.stopSound() }

		if (nextActivePlayer.isPlayingSound)
			nextActivePlayer.stopSound()
		else
			nextActivePlayer.playSound()
		this.adapter.notifyDataSetChanged()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		val player = event.player
		if (this.values.contains(player) && !event.isAlive) { // removed a destroyed media player
			val index = this.values.indexOf(player)
			this.manager.remove(listOf(player))
			this.adapter.notifyItemRemoved(index)
		}
		else
			this.adapter.notifyDataSetChanged()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerCompletedEvent) {
		val finishedPlayerData = event.player.mediaPlayerData

		if (this.currentItemIndex != INDEX_NOT_SET) {
			val currentPlayer = this.values[this.currentItemIndex].mediaPlayerData
			if (currentPlayer !== finishedPlayerData) // finished player was not the current player
				return

			this.currentItemIndex += 1
			if (this.values.isEmpty()) {
				this.currentItemIndex = INDEX_NOT_SET
				return
			}

			if (this.currentItemIndex >= this.values.size)
				this.currentItemIndex = 0

			this.values[this.currentItemIndex].playSound()
			this.adapter.notifyDataSetChanged()
		}
	}
}
