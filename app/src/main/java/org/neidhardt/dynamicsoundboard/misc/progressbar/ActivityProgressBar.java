package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.content.Context;
import android.util.AttributeSet;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by eric.neidhardt on 22.05.2015.
 */
public class ActivityProgressBar extends SmoothProgressBar
{
	private ActivityProgressBarPresenter presenter;

	public ActivityProgressBar(Context context)
	{
		super(context);
		this.init();
	}

	public ActivityProgressBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init();
	}

	public ActivityProgressBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init();
	}

	private void init()
	{
		this.presenter = new ActivityProgressBarPresenter();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.presenter.setView(this);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.presenter.onAttachToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow();
		super.onDetachedFromWindow();
	}
}
