package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.support.v7.widget.RecyclerView;
import com.emtronics.dragsortrecycler.DragSortRecycler;

/**
 * This class is used to intercept scroll events and pass events to drag-drop recycler its scroll listener
 */
class SoundSheetScrollListener extends RecyclerView.OnScrollListener
{
	private DragSortRecycler dragSortRecycler;
	private RecyclerView soundLayout;

	private boolean isDragInProgress = false;

	public SoundSheetScrollListener(DragSortRecycler dragSortRecycler)
	{
		this.dragSortRecycler = dragSortRecycler;
	}

	public void setSoundLayout(RecyclerView soundLayout)
	{
		this.soundLayout = soundLayout;
		this.soundLayout.setOnScrollListener(this);
	}

	public void setIsDragInProgress(boolean isDragInProgress)
	{
		this.isDragInProgress = isDragInProgress;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState)
	{
		super.onScrollStateChanged(recyclerView, newState);
		if (this.isDragInProgress) // if drag/drop currently in progress then pass the event to the DragSortRecycler
			dragSortRecycler.getScrollListener().onScrollStateChanged(recyclerView, newState);

		if (this.soundLayout == null) // should not happen
			return;

		if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) // Recycler view is not idle
		{
			if (!this.isDragInProgress)
				soundLayout.removeOnItemTouchListener(dragSortRecycler); // if view is not currently dragged, we remove the DragSortRecycler to prevent undesired touching
		}
		else
		{
			soundLayout.addOnItemTouchListener(dragSortRecycler);
		}
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy)
	{
		super.onScrolled(recyclerView, dx, dy);
		dragSortRecycler.getScrollListener().onScrolled(recyclerView, dx, dy);
	}
}
