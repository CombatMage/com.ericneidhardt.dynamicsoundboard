package org.neidhardt.dynamicsoundboard.views;


import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundAdapter;

public abstract class DismissibleItemViewHolder
		extends
			RecyclerView.ViewHolder
		implements
			ViewPager.OnPageChangeListener,
			View.OnClickListener
{
	private final TextView deleteSoundInfoLeft;
	private final TextView deleteSoundInfoRight;

	private ViewPager viewPager;

	public DismissibleItemViewHolder(View itemView)
	{
		super(itemView);

		this.viewPager = (ViewPager)itemView;
		this.viewPager.setOffscreenPageLimit(2);
		this.viewPager.setAdapter(this.getPagerAdapter());
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());

		this.deleteSoundInfoLeft = (TextView) itemView.findViewById(R.id.tv_delete_sound_left);
		this.deleteSoundInfoLeft.setOnClickListener(this);
		this.deleteSoundInfoRight = (TextView) itemView.findViewById(R.id.tv_delete_sound_right);
		this.deleteSoundInfoRight.setOnClickListener(this);
	}

	protected void bindData(int positionInDataSet)
	{
		boolean isOneSwipeDeleteEnabled = SoundboardPreferences.isOneSwipeToDeleteEnabled();
		if (isOneSwipeDeleteEnabled)
		{
			this.deleteSoundInfoLeft.setText(R.string.sound_control_delete);
			this.deleteSoundInfoRight.setText(R.string.sound_control_delete);
		}
		else
		{
			this.deleteSoundInfoLeft.setText(R.string.sound_control_delete_confirm);
			this.deleteSoundInfoRight.setText(R.string.sound_control_delete_confirm);
		}
	}

	@Override
	public void onClick(View view)
	{
		boolean isOneSwipeDeleteEnabled = SoundboardPreferences.isOneSwipeToDeleteEnabled();
		if (isOneSwipeDeleteEnabled)
			return;

		int id = view.getId();
		if (id == this.deleteSoundInfoLeft.getId() || id == this.deleteSoundInfoRight.getId())
			this.delete();
	}

	@Override
	public void onPageSelected(final int selectedPage)
	{
		getHandler().postDelayed(new Runnable() // delay deletion, because page is selected before scrolling has settled
		{
			@Override
			public void run() {
				boolean isOneSwipeDeleteEnabled = SoundboardPreferences.isOneSwipeToDeleteEnabled();
				if (selectedPage != getIndexOfContentPage())
				{
					if (isOneSwipeDeleteEnabled)
						delete();
				}
			}
		}, SoundAdapter.UPDATE_INTERVAL);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{}

	protected abstract Handler getHandler();

	protected abstract void delete();

	protected abstract int getIndexOfContentPage();

	protected abstract PagerAdapter getPagerAdapter();

	protected void resetViewPager()
	{
		this.viewPager.setCurrentItem(this.getIndexOfContentPage());
	}
}
