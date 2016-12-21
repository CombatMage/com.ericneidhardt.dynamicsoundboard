package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.eventbus_utils.registerIfRequired
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
fun createSoundPresenter(
		fragmentTag: String,
		eventBus: EventBus,
		onItemDeletionRequested: (PendingDeletionHandler, Int) -> Unit,
		recyclerView: RecyclerView): SoundPresenter
{
	return SoundPresenter(
			fragmentTag = fragmentTag,
			eventBus = eventBus,
			soundsDataAccess = soundsDataAccess
	).apply {
		val deletionHandler = PendingDeletionHandler(this, soundsDataStorage, onItemDeletionRequested)
		val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(recyclerView.context, deletionHandler,
				this, fragmentTag, soundsDataStorage)).apply { this.attachToRecyclerView(recyclerView) }
		val adapter = SoundAdapter(itemTouchHelper, this, soundsDataStorage, eventBus)
		this.adapter = adapter
	}
}

class SoundPresenter
(
		val soundSheet: NewSoundSheet,
		private val eventBus: EventBus,
		private val newSoundManager: NewSoundManager
) :
		OnSoundsChangedEventListener,
		MediaPlayerEventListener
{
	private val TAG = javaClass.name

	private var subscriptions = CompositeSubscription()
	var adapter: SoundAdapter? = null
	val values: List<MediaPlayerController> get() =
			this.newSoundManager.sounds.getOrElse(this.soundSheet, { emptyList() } )

	fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)
		this.adapter?.notifyDataSetChanged()
		this.subscriptions = CompositeSubscription()
		RxSoundManager.changesSoundList(this.newSoundManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() }
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

	@Subscribe
	override fun onEvent(event: MediaPlayerCompletedEvent) {
		Logger.d(TAG, "onEvent :" + event)
	}
}
