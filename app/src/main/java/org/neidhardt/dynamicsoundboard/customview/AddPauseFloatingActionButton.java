package org.neidhardt.dynamicsoundboard.customview;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils;

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
		if (this.isStatePause)
			return;
		this.isStatePause = true;
		refreshDrawableState();

		this.post(this);
	}

	public void setAddState()
	{
		if (!this.isStatePause)
			return;
		this.isStatePause = false;
		refreshDrawableState();

		this.post(this);
	}

	@Override
	public void run()
	{
		Animator animator = AnimationUtils.createCircularRevealIfAvailable(AddPauseFloatingActionButton.this,
				getWidth(),
				getHeight(),
				0,
				getHeight() * 2);

		if (animator != null)
			animator.start();
	}
}
