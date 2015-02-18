package org.neidhardt.dynamicsoundboard.customview;


import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundAdapter;

public abstract class DismissibleItemViewHolder
		extends
			RecyclerView.ViewHolder
		implements
			ViewPager.OnPageChangeListener
{
	private ViewPager viewPager;
	private boolean isOneSwipeDeleteEnabled = false;

	public DismissibleItemViewHolder(View itemView)
	{
		super(itemView);

		this.viewPager = (ViewPager)itemView;
		this.viewPager.setOffscreenPageLimit(2);
		this.viewPager.setAdapter(this.getPagerAdapter());
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());
		this.isOneSwipeDeleteEnabled = SoundboardPreferences.isOneSwipeToDeleteEnabled();
	}

	@Override
	public void onPageSelected(final int selectedPage)
	{
		getHandler().postDelayed(new Runnable() // delay deletion, because page is selected before scrolling has settled
		{
			@Override
			public void run() {
				if (selectedPage != getIndexOfContentPage() && isOneSwipeDeleteEnabled)
					delete();
			}
		}, SoundAdapter.UPDATE_INTERVAL);
	}

	protected abstract Handler getHandler();

	protected abstract void delete();

	protected abstract int getIndexOfContentPage();

	protected abstract PagerAdapter getPagerAdapter();

	protected void resetViewPager()
	{
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());
	}
}
