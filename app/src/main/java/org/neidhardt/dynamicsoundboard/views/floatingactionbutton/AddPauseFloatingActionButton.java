package org.neidhardt.dynamicsoundboard.views.floatingactionbutton;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils;

/**
 * File created by Eric Neidhardt on 12.11.2014.
 */
public class AddPauseFloatingActionButton extends com.melnykov.fab.FloatingActionButton implements Runnable, View.OnClickListener
{
	private static final int[] PAUSE_STATE = new int[] { R.attr.state_pause };

	private AddPauseFloatingActionButtonPresenter presenter;

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context)
	{
		super(context);
		this.init();
	}

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init();
	}

	@SuppressWarnings("unused")
	public AddPauseFloatingActionButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init();
	}

	private void init()
	{
		this.presenter = new AddPauseFloatingActionButtonPresenter(DynamicSoundboardApplication.getApplicationComponent().provideSoundsDataAccess());
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.setOnClickListener(this);
		this.presenter.setView(this);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.presenter.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow();
		super.onDetachedFromWindow();
	}

	@Override
	public void onClick(View v)
	{
		this.presenter.onFabClicked();
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace)
	{
		int[] state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.length);
		if (this.presenter != null && this.presenter.isStatePause())
			mergeDrawableStates(state, PAUSE_STATE);

		return state;
	}

	void animateUiChanges()
	{
		this.post(this);
	}

	@Override
	public void run()
	{
		Animator animator = AnimationUtils.createCircularReveal(AddPauseFloatingActionButton.this,
				getWidth(),
				getHeight(),
				0,
				getHeight() * 2);

		if (animator != null)
			animator.start();
	}
}
