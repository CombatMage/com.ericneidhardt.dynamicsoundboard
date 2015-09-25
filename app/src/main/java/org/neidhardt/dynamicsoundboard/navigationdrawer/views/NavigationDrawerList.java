package org.neidhardt.dynamicsoundboard.navigationdrawer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


public abstract class NavigationDrawerList extends FrameLayout
{
	public NavigationDrawerList(Context context)
	{
		super(context);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	protected abstract int getItemCount();

	protected abstract int getActionModeTitle();

	public abstract NavigationDrawerListPresenter getPresenter();
}
