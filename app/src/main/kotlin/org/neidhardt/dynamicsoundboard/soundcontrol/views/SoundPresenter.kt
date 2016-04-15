package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable
import java.util.*

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
fun createSoundPresenter(
		fragmentTag: String,
		eventBus: EventBus,
		recyclerView: RecyclerView,
		soundsDataAccess: SoundsDataAccess,
		soundsDataStorage: SoundsDataStorage): SoundPresenter
{
	return SoundPresenter(
			fragmentTag = fragmentTag,
			eventBus = eventBus,
			soundsDataAccess = soundsDataAccess,
			soundsDataStorage = soundsDataStorage
	).apply {
		val adapter = SoundAdapter(this, soundsDataStorage, eventBus)
		this.adapter = adapter
		ItemTouchHelper(ItemTouchCallback(this, fragmentTag, soundsDataStorage)).attachToRecyclerView(recyclerView)
	}
}

private val DELETION_TIMEOUT = 5000.toLong()

class SoundPresenter
(
		private val fragmentTag: String,
		private val eventBus: EventBus,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage
) :
		OnSoundsChangedEventListener,
		MediaPlayerEventListener
{
	private val TAG = javaClass.name

	private val handler = EnhancedHandler()
	private val pendingDeletions: MutableMap<MediaPlayerController, KillableRunnable> = HashMap()

	var adapter: SoundAdapter? = null
	val values: MutableList<MediaPlayerController> = ArrayList()

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

	fun setProgressUpdateTimer(startTime: Boolean)
	{
		if (startTime)
			this.adapter?.startProgressUpdateTimer()
		else
			this.adapter?.stopProgressUpdateTimer()
	}

	fun onItemDeletionRequested(item: MediaPlayerController)
	{
		item.isDeletionPending = true
		if (item.isPlayingSound) item.stopSound()
		val pendingDeletionTask = object : KillableRunnable()
		{
			override fun call() { soundsDataStorage.removeSounds(listOf(item)) }
		}
		this.pendingDeletions[item] = pendingDeletionTask
		this.handler.postDelayed(pendingDeletionTask, DELETION_TIMEOUT)
	}

	fun cancelItemDeletion(item: MediaPlayerController)
	{
		val deletionTask = this.pendingDeletions[item]
		this.handler.removeCallbacks(deletionTask)

		item.isDeletionPending = false
		this.adapter?.notifyItemChanged(item)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		val playerId = event.playerId
		val players = this.values
		val count = players.size
		for (i in 0..count - 1)
		{
			val player = players[i]
			if (event.isAlive && player.mediaPlayerData.playerId == playerId && !player.isDeletionPending)
				this.adapter?.notifyItemChanged(i)
		}
	}

	@Subscribe
	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		Logger.d(TAG, "onEvent :" + event)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundAddedEvent)
	{
		val newPlayer = event.player
		if (newPlayer.mediaPlayerData.fragmentTag.equals(this.fragmentTag))
		{
			val count = this.values.size
			val positionToInsert = newPlayer.mediaPlayerData.sortOrder
			if (positionToInsert == null)
			{
				newPlayer.mediaPlayerData.sortOrder = count
				newPlayer.mediaPlayerData.updateItemInDatabaseAsync()
				this.insertPlayer(count, newPlayer) // append to end of list
			}
			else
			{
				for (i in 0..count - 1)
				{
					val existingPlayer = this.values[i]
					if (positionToInsert < existingPlayer.mediaPlayerData.sortOrder)
					{
						this.insertPlayer(i, newPlayer)
						return
					}
				}
				this.insertPlayer(count, newPlayer) // append to end of list
			}
		}
	}

	private fun insertPlayer(position: Int, player: MediaPlayerController)
	{
		this.values.add(position, player)
		this.adapter?.notifyItemInserted(position)
		if (position == this.values.size - 1)
			this.adapter?.notifyItemChanged(position - 1)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundMovedEvent)
	{
		val movedPlayer = event.player
		if (movedPlayer.mediaPlayerData.fragmentTag.equals(this.fragmentTag))
		{
			this.values.removeAt(event.from)
			this.values.add(event.to, movedPlayer)

			val start = Math.min(event.from, event.to); // we need to update all sound after the moved one
			val end = Math.max(event.from, event.to);

			for (i in start..end)
			{
				val playerData = this.values[i].mediaPlayerData
				playerData.sortOrder = i;
				playerData.updateItemInDatabaseAsync();
			}

			this.adapter?.notifyItemMoved(event.from, event.to)
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundsRemovedEvent)
	{
		if (event.removeAll())
			this.adapter?.notifyDataSetChanged()
		else
		{
			val players = event.players
			for (player in players.orEmpty())
				this.removePlayerAndUpdateSortOrder(player)
		}
	}

	private fun removePlayerAndUpdateSortOrder(player: MediaPlayerController)
	{
		val index = this.values.indexOf(player)
		if (index != -1)
		{
			this.values.remove(player)
			this.adapter?.notifyItemRemoved(index)

			this.updateSortOrdersAfter(index - 1) // -1 to ensure item at index (which was index + 1 before) is also updated
		}
	}

	private fun updateSortOrdersAfter(index: Int)
	{
		val count = this.values.size
		for (i in index + 1 .. count - 1)
		{
			val playerData = this.values[i].mediaPlayerData
			val sortOrder = playerData.sortOrder
			playerData.sortOrder = sortOrder - 1;
			playerData.updateItemInDatabaseAsync();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundChangedEvent)
	{
		val player = event.player
		val index = this.values.indexOf(player)
		if (index != -1)
			this.adapter?.notifyItemChanged(index)
	}
}

class ItemTouchCallback
(
		private val presenter: SoundPresenter,
		private val fragmentTag: String,
		private val soundsDataStorage: SoundsDataStorage
) : ItemTouchHelper.Callback()
{
	override fun isLongPressDragEnabled(): Boolean = true

	override fun isItemViewSwipeEnabled(): Boolean = true

	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int
	{
		val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
		val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
		return makeMovementFlags(dragFlags, swipeFlags)
	}

	override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
	{
		val from = viewHolder.adapterPosition
		val to = target.adapterPosition
		if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
		this.soundsDataStorage.moveSoundInFragment(this.fragmentTag, from, to)
		return true
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
	{
		val position = viewHolder.adapterPosition
		if (position != RecyclerView.NO_POSITION)
		{
			val item = this.presenter.values[position]
			if (SoundboardPreferences.isOneSwipeToDeleteEnabled)
				this.soundsDataStorage.removeSounds(listOf(item))
			else
				this.presenter.onItemDeletionRequested(item)
		}
	}

}