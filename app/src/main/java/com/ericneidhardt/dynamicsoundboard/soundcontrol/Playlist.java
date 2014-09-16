package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class Playlist extends FrameLayout
{
	public Playlist(Context context)
	{
		super(context);
		this.inflateLayout(context);
	}

	public Playlist(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context);
	}

	public Playlist(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.inflateLayout(context);
	}

	private void inflateLayout(Context context)
	{

	}

	private void initRecycleView(Context context)
	{
		/*this.addItemDecoration(new DividerItemDecoration(context,
				DividerItemDecoration.VERTICAL_LIST, null));
		this.setLayoutManager(new LinearLayoutManager(context));
		this.setItemAnimator(new DefaultItemAnimator());*/
	}
}
