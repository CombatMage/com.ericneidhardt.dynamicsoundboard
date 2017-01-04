package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.eventbus_utils.registerIfRequired
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
fun createSoundPresenter(
		soundSheet: NewSoundSheet,
		eventBus: EventBus,
		onItemDeletionRequested: (PendingDeletionHandler, Int) -> Unit,
		soundManager: SoundManager,
		playlistManager: PlaylistManager,
		recyclerView: RecyclerView
): SoundPresenter
{
	return SoundPresenter(
			soundSheet = soundSheet,
			eventBus = eventBus,
			soundManager = soundManager,
			playlistManager = playlistManager
	).apply {
		val deletionHandler = PendingDeletionHandler(this, soundManager, onItemDeletionRequested)
		val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(recyclerView.context, deletionHandler,
				this, soundSheet, soundManager)).apply { this.attachToRecyclerView(recyclerView) }

		val adapter = SoundAdapter(itemTouchHelper, this, playlistManager, eventBus)
		this.adapter = adapter
	}
}

class SoundPresenter (
		val soundSheet: NewSoundSheet,
		private val eventBus: EventBus,
		private val soundManager: SoundManager,
		private val playlistManager: PlaylistManager
) : MediaPlayerEventListener {

	private var subscriptions = CompositeSubscription()

	var adapter: SoundAdapter? = null
	val values: List<MediaPlayerController> get() {
		val players = this.soundManager.sounds.getOrElse(this.soundSheet, { emptyList() } )
		return players
	}

	fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)
		this.adapter?.notifyDataSetChanged()

		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(RxSoundManager.changesSoundList(this.soundManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })

		this.subscriptions.add(RxSoundManager.movesSoundInList(this.soundManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { event ->
				//	this.adapter?.notifyItemMoved(event.from, event.to)
				})

		this.subscriptions.add(RxNewPlaylistManager.playlistChanges(this.playlistManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })
	}

	fun onDetachedFromWindow() {
		this.eventBus.unregister(this)
		this.subscriptions.unsubscribe()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		val playerId = event.playerId
		val players = this.values
		val count = players.size
		for (i in 0..count - 1) {
			val player = players[i]
			if (event.isAlive && player.mediaPlayerData.playerId == playerId && !player.isDeletionPending)
				this.adapter?.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}
