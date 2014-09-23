package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;


public abstract class NavigationDrawerListL extends FrameLayout
{
	protected NavigationDrawerFragment parent;

	public NavigationDrawerListL(Context context) {
		super(context);
	}

	public NavigationDrawerListL(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavigationDrawerListL(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void prepareItemDeletion()
	{
		if (this.parent == null)
			throw new NullPointerException("Cannot prepare deletion, because the containing fragment is null");



	}
}
