package org.neidhardt.dynamicsoundboard.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewAnimationUtils;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.Util;

/**
 * Created by Eric Neidhardt on 12.11.2014.
 */
public class AddPauseFloatingActionButton extends com.melnykov.fab.FloatingActionButton implements Runnable
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
		if (Util.IS_LOLLIPOP_AVAILABLE) // reveal animation is only available on lollipop
			this.post(this);
	}

	public void setAddState()
	{
		this.isStatePause = false;
		refreshDrawableState();
		if (Util.IS_LOLLIPOP_AVAILABLE) // reveal animation is only available on lollipop
			this.post(this);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void run()
	{
		ViewAnimationUtils.createCircularReveal(AddPauseFloatingActionButton.this,
				getWidth(),
				getHeight(),
				0,
				getHeight() * 2).start();
	}
}
