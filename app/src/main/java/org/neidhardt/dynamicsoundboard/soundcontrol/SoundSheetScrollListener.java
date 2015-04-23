package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.support.v7.widget.RecyclerView;
import com.emtronics.dragsortrecycler.DragSortRecycler;

/**
 * This class is used to intercept scroll events and pass events to drag-drop recycler its scroll listener
 */
class SoundSheetScrollListener extends RecyclerView.OnScrollListener
{
	private SoundSortRecycler dragSortRecycler;

	public SoundSheetScrollListener(SoundSortRecycler dragSortRecycler)
	{
		this.dragSortRecycler = dragSortRecycler;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState)
	{
		super.onScrollStateChanged(recyclerView, newState);
		if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) // Recycler view is not idle
		{
			this.dragSortRecycler.setViewHandleId(0);
		}
		else
		{
			this.dragSortRecycler.setViewHandleId(this.dragSortRecycler.getDragViewId());
		}
	}

}
