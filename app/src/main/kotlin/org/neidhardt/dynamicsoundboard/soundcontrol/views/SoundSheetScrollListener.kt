package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView

/**
 * This class is used to intercept scroll events and pass events to drag-drop recycler its scroll listener
 */
internal class SoundSheetScrollListener(private val dragSortRecycler: SoundDragSortRecycler) : RecyclerView.OnScrollListener()
{

	override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int)
	{
		super.onScrollStateChanged(recyclerView, newState)
		if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING)// Recycler view is not idle
			this.dragSortRecycler.setViewHandleId(0)
		else
			this.dragSortRecycler.setViewHandleId(this.dragSortRecycler.dragViewId)
	}
}
