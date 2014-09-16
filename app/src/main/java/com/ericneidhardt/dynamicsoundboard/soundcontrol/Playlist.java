package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;

public class Playlist extends RecyclerView
{
	public Playlist(Context context)
	{
		super(context);
		this.init(context);
	}

	public Playlist(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	public Playlist(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init(context);
	}

	private void init(Context context)
	{
		this.addItemDecoration(new DividerItemDecoration(context,
				DividerItemDecoration.VERTICAL_LIST, null));
		this.setLayoutManager(new LinearLayoutManager(context));
		this.setItemAnimator(new DefaultItemAnimator());
	}
}
