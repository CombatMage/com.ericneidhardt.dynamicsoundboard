package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage

/**
 * File created by eric.neidhardt on 14.04.2016.
 */
class DragDropItemTouchCallback
(
		private val adapter: SoundAdapter,
		private val fragmentTag: String,
		private val soundsDataStorage: SoundsDataStorage
) : ItemTouchHelper.Callback()
{
	override fun isLongPressDragEnabled(): Boolean = true

	override fun isItemViewSwipeEnabled(): Boolean = true

	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int
	{
		val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
		return dragFlags
	}

	override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
	{
		val from = viewHolder.adapterPosition
		val to = target.adapterPosition
		if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false

		this.soundsDataStorage.moveSoundInFragment(this.fragmentTag, from, to)
		this.adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
		return true
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int)
	{
		throw UnsupportedOperationException()
	}
}