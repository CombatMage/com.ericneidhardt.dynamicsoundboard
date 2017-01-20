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
		soundManager: SoundManager,
		playlistManager: PlaylistManager,
		recyclerView: RecyclerView,

		onItemDeletionRequested: (PendingDeletionHandler, Int) -> Unit,
		onTogglePlaylistClicked: (addToPlaylist: Boolean, MediaPlayerController) -> Unit,
		onSoundNamedEdited: (name: String, MediaPlayerController) -> Unit,
		onOpenSettingsClicked: (MediaPlayerController) -> Unit
): SoundPresenter
{
	val presenter = SoundPresenter(
			soundSheet = soundSheet,
			soundManager = soundManager,
			playlistManager = playlistManager
	)

	val deletionHandler = PendingDeletionHandler(
			soundPresenter = presenter,
			manager = soundManager,
			onItemDeletionRequested = onItemDeletionRequested
	)

	val itemTouchHelper = ItemTouchHelper(
				ItemTouchCallback(
						context = recyclerView.context,
						deletionHandler = deletionHandler,
						presenter = presenter,
						soundSheet = soundSheet,
						soundManager = soundManager
				)
	)
	itemTouchHelper.attachToRecyclerView(recyclerView)

	val adapter = SoundAdapter(
			itemTouchHelper = itemTouchHelper,
			presenter = presenter,
			playlistManager = playlistManager,
			onTogglePlaylistClicked = onTogglePlaylistClicked,
			onSoundNamedEdited = onSoundNamedEdited,
			onOpenSettingsClicked = onOpenSettingsClicked)

	presenter.adapter = adapter

	return presenter
}

class SoundPresenter (
		val soundSheet: NewSoundSheet,
		private val soundManager: SoundManager,
		private val playlistManager: PlaylistManager
) : MediaPlayerEventListener {

	private val eventBus = EventBus.getDefault()

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
