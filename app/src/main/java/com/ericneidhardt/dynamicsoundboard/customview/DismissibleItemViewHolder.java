package com.ericneidhardt.dynamicsoundboard.customview;


import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class DismissibleItemViewHolder
		extends
			RecyclerView.ViewHolder
		implements
			ViewPager.OnPageChangeListener
{
	public DismissibleItemViewHolder(View itemView)
	{
		super(itemView);

		ViewPager viewPager = (ViewPager)itemView;
		viewPager.setOffscreenPageLimit(2);
		viewPager.setAdapter(this.getPagerAdapter());
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(this.getIndexOfContentPage());
	}

	protected abstract int getIndexOfContentPage();

	protected abstract PagerAdapter getPagerAdapter();
}
