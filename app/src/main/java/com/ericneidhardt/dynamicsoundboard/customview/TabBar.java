package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by Eric Neidhardt on 31.08.2014.
 */
public class TabBar extends LinearLayout
{
	public TabBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_tab_bar, this, true);
	}
}
