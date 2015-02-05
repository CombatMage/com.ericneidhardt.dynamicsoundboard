package org.neidhardt.dynamicsoundboard.customview;


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
	private ViewPager viewPager;

	public DismissibleItemViewHolder(View itemView)
	{
		super(itemView);

		this.viewPager = (ViewPager)itemView;
		this.viewPager.setOffscreenPageLimit(2);
		this.viewPager.setAdapter(this.getPagerAdapter());
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());
	}

	protected abstract int getIndexOfContentPage();

	protected abstract PagerAdapter getPagerAdapter();

	protected void resetViewPager()
	{
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());
	}
}
