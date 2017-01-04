package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences

/**
 * @author eric.neidhardt on 14.06.2016.
 */
class ItemTouchCallback
(
		context: Context,
		private val deletionHandler: PendingDeletionHandler,
		private val presenter: SoundPresenter,
		private val soundSheet: NewSoundSheet,
		private val soundManager: SoundManager
) : ItemTouchHelper.Callback() {

	private val handler = Handler()

	override fun isLongPressDragEnabled(): Boolean = false

	override fun isItemViewSwipeEnabled(): Boolean = true

	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
		val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
		val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
		return makeMovementFlags(dragFlags, swipeFlags)
	}

	override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
		val targetPosition = target.adapterPosition
		if (targetPosition != RecyclerView.NO_POSITION) recyclerView.scrollToPosition(targetPosition)
		return super.canDropOver(recyclerView, current, target)
	}

	override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
		val from = viewHolder.adapterPosition
		val to = target.adapterPosition
		if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
		this.soundManager.move(this.soundSheet, from, to)
		return true
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
		val position = viewHolder.adapterPosition
		if (position != RecyclerView.NO_POSITION) {
			val item = this.presenter.values[position]

			if (SoundboardPreferences.isOneSwipeToDeleteEnabled)
				this.handler.postDelayed({ this.soundManager.remove(this.soundSheet, listOf(item)) }, 200) // give animation some time to settle
			else
				this.deletionHandler.requestItemDeletion(item)
		}
	}

	private val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_delete)

	private val backgroundPaint = Paint().apply {
		this.style = Paint.Style.FILL
		this.color = ContextCompat.getColor(context, R.color.primary_200)
	}

	private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
		val size = context.resources.getDimension(R.dimen.text_title)
		this.textSize = size
		this.color = ContextCompat.getColor(context, R.color.text_header)
		this.textAlign = Paint.Align.LEFT
	}

	override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
							 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
		super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

		val view = viewHolder.itemView
		val resources = view.context.resources
		val left = view.left.toFloat()
		val top = view.top.toFloat()
		val height = view.height.toFloat()
		val width = view.width.toFloat()
		val margin = resources.getDimension(R.dimen.margin_default)

		val heightBitmap = (height / 2) - (bitmap.height / 2)
		val heightText = (height / 2) - (textPaint.textSize / 2) + margin

		if (!isCurrentlyActive && actionState != ItemTouchHelper.ACTION_STATE_DRAG) {
			canvas.drawRect(left, top, width, top + height, backgroundPaint)
			canvas.drawText(resources.getString(R.string.sound_control_deletion_pending_single), margin, top + heightText, textPaint)
		} else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			if (dX > 0) // swiping right
			{
				canvas.drawRect(left, top, dX, top + height, backgroundPaint)
				canvas.drawBitmap(bitmap, margin, top + heightBitmap, null)
			} else // swiping left
			{
				canvas.drawRect(width + dX, top, width, top + height, backgroundPaint)
				canvas.drawBitmap(bitmap, (width - bitmap.width) - margin, top + heightBitmap, null)
			}
		}
	}
}