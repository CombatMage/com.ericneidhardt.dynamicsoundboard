package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.SnackbarPresenter
import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable
import java.util.*

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
fun createSoundPresenter(
		fragmentTag: String,
		eventBus: EventBus,
		snackbarPresenter: SnackbarPresenter,
		recyclerView: RecyclerView,
		soundsDataAccess: SoundsDataAccess,
		soundsDataStorage: SoundsDataStorage): SoundPresenter
{
	return SoundPresenter(
			fragmentTag = fragmentTag,
			eventBus = eventBus,
			soundsDataAccess = soundsDataAccess
	).apply {
		val adapter = SoundAdapter(this, soundsDataStorage, eventBus)
		val deletionHandler = PendingDeletionHandler(adapter, snackbarPresenter, soundsDataStorage)
		this.adapter = adapter
		ItemTouchHelper(ItemTouchCallback(deletionHandler, this, fragmentTag, soundsDataStorage)).attachToRecyclerView(recyclerView)
	}
}

class SoundPresenter
(
		private val fragmentTag: String,
		private val eventBus: EventBus,
		private val soundsDataAccess: SoundsDataAccess
) :
		OnSoundsChangedEventListener,
		MediaPlayerEventListener
{
	private val TAG = javaClass.name

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

private val DELETION_TIMEOUT = 5000

private class PendingDeletionHandler
(
		private val soundAdapter: SoundAdapter,
		private val snackbarPresenter: SnackbarPresenter,
		private val soundsDataStorage: SoundsDataStorage
)
{
	private val handler = EnhancedHandler()
	private var deletionTask: KillableRunnable? = null
	private val pendingDeletions = ArrayList<MediaPlayerController>()

	private var snackbar: Snackbar? = null
	private val snackbarAction = SnackbarPresenter.SnackbarAction(R.string.sound_control_deletion_pending_undo, { this.restoreDeletedItems() } )

	fun requestItemDeletion(item: MediaPlayerController)
	{
		this.deletionTask?.apply { handler.removeCallbacks(this) }

		item.isDeletionPending = true
		if (item.isPlayingSound) item.stopSound()

		this.pendingDeletions.add(item)
		this.deletionTask = object : KillableRunnable()
		{
			override fun call() { deletePendingItems() }
		}.apply { handler.postDelayed(this, DELETION_TIMEOUT.toLong()) }

		this.snackbar = this.snackbarPresenter.makeSnackbarForDeletion(this.pendingDeletions.size,
				DELETION_TIMEOUT, this.snackbarAction).apply { this.show() }
	}

	private fun deletePendingItems()
	{
		this.deletionTask?.apply { handler.removeCallbacks(this) }
		this.soundsDataStorage.removeSounds(this.pendingDeletions)
		this.snackbar?.dismiss()
		this.pendingDeletions.clear()
	}

	private fun restoreDeletedItems()
	{
		this.deletionTask?.apply { handler.removeCallbacks(this) }
		this.pendingDeletions.map { item ->
			item.isDeletionPending = false
			soundAdapter.notifyItemChanged(item)
		}
		this.pendingDeletions.clear()
	}
}

private fun SnackbarPresenter.makeSnackbarForDeletion(count: Int, duration: Int, action: SnackbarPresenter.SnackbarAction?): Snackbar
{
	val message = if (count == 1)
			this.coordinatorLayout.context.resources.getString(R.string.sound_control_deletion_pending_single)
		else
			this.coordinatorLayout.context.resources.getString(R.string.sound_control_deletion_pending).replace("{%s0}", count.toString())
	return this.makeSnackbar(message, duration, action)
}

private class ItemTouchCallback
(
		private val deletionHandler: PendingDeletionHandler,
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

	override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
	{
		val targetPosition = target.adapterPosition
		if (targetPosition != RecyclerView.NO_POSITION) recyclerView.scrollToPosition(targetPosition)
		return super.canDropOver(recyclerView, current, target)
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
				this.deletionHandler.requestItemDeletion(item)
		}
	}

	private val backgroundPaint = Paint().apply {
		this.style = Paint.Style.FILL
		this.color = ContextCompat.getColor(SoundboardApplication.context, R.color.primary_200)
	}

	private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
		val context = SoundboardApplication.context
		val size = context.resources.getDimension(R.dimen.text_body)
		this.textSize = size
		this.color = ContextCompat.getColor(context, R.color.primary_500)
		this.textAlign = Paint.Align.LEFT
	}

	override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
							 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
	{
		super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

		val view = viewHolder.itemView
		val ressources = view.context.resources
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
		{
			val left = view.left.toFloat()
			val top = view.top.toFloat()
			val height = view.height.toFloat()
			val width = view.width.toFloat()

			if (dX > 0) // swiping right
			{
				canvas.drawRect(left, top, dX, top + height, backgroundPaint)

				val bitmap = BitmapFactory.decodeResource(ressources, R.drawable.ic_action_add_sound)
				val height = (view.getHeight() / 2) - (bitmap.getHeight() / 2)
				canvas.drawBitmap(bitmap, 96f, view.getTop().toFloat() + height, null)
			}
			else
			{ // swiping left
				canvas.drawRect(width + dX, top, width, top + height, backgroundPaint)

				/*paint.setColor(view.context.getResources().getColor(R.color.accent_400));
				bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_circle_bin);
				float height = (itemView.getHeight() / 2) - (bitmap.getHeight() / 2);
				float bitmapWidth = bitmap.getWidth();


				c.drawBitmap(bitmap, ((float) itemView.getRight() - bitmapWidth) - 96f, (float) itemView.getTop() + height, null);*/
			}
		}

		//val bitmap = BitmapFactory.decodeResource(view.context.resources, R.drawable.ic_action_add_sound)
		//canvas.drawBitmap(bitmap, left.toFloat(), top.toFloat() + height, Paint().apply { this.color = Color.BLUE })


		//val baseLine = (view.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)
		//canvas.drawText("Sound wird gelöscht", left.toFloat(), bottom.toFloat(), textPaint)
	}
}