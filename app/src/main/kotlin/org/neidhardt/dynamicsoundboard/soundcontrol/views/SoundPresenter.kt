package org.neidhardt.dynamicsoundboard.soundcontrol.views

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment
import org.neidhardt.eventbus_utils.registerIfRequired
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.lang.ref.WeakReference

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
class SoundPresenter (
		private val soundManager: SoundManager,
		private val playlistManager: PlaylistManager,
		fragment: SoundSheetFragment
) : MediaPlayerEventListener {

	private val fragment: WeakReference<SoundSheetFragment>

	init { this.fragment = WeakReference(fragment) }

	private val eventBus = EventBus.getDefault()
	private val adapter: SoundAdapter? get() = this.fragment.get().soundAdapter

	private var subscriptions = CompositeSubscription()

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
					// nothing to do, item was already moved via ItemTouchHelper
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
		val players = this.adapter?.values ?: return
		val count = players.size
		for (i in 0..count - 1) {
			val player = players[i]
			if (event.isAlive && player.mediaPlayerData.playerId == playerId && !player.isDeletionPending)
				this.adapter?.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}
