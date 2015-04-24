package com.emtronics.dragsortrecycler;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by eric.neidhardt on 24.04.2015.
 */
public class LongTouchDetector extends GestureDetector.SimpleOnGestureListener
{
	private RecyclerView recyclerView;
	private OnLongTouchListener onLongTouchListener;

	public void setOnLongTouchListener(OnLongTouchListener onLongTouchListener)
	{
		this.onLongTouchListener = onLongTouchListener;
	}

	public void setRecyclerView(RecyclerView recyclerView)
	{
		this.recyclerView = recyclerView;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		if (this.recyclerView != null) {
			View childView = this.recyclerView.findChildViewUnder(e.getX(), e.getY());
			if (childView != null && this.onLongTouchListener != null)
			{
				this.onLongTouchListener.onLongTouch(e);
			}
		}
	}

	public interface OnLongTouchListener
	{
		void onLongTouch(MotionEvent event);
	}
}
