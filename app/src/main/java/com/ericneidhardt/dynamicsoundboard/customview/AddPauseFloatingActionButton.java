package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by Eric Neidhardt on 12.11.2014.
 */
public class AddPauseFloatingActionButton extends com.melnykov.fab.FloatingActionButton
{
	private static final int[] PAUSE_STATE = new int[] { R.attr.state_pause };

	private boolean isStatePause = false;

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context)
	{
		super(context);
	}

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace)
	{
		int[] state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.length);
		if (this.isStatePause)
			mergeDrawableStates(state, PAUSE_STATE);

		return state;
	}

	public void setPauseState()
	{
		this.isStatePause = true;
		refreshDrawableState();
	}

	public void setAddState()
	{
		this.isStatePause = false;
		refreshDrawableState();
	}
}
