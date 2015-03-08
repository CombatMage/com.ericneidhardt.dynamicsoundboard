package org.neidhardt.dynamicsoundboard.soundlayouts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.navigationdrawer.NavigationDrawerList;

/**
 * Created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayoutList extends NavigationDrawerList implements SoundLayoutListAdapter.OnItemClickListener
{
	private Interpolator animationInterpolator = new AccelerateDecelerateInterpolator();

	private SoundLayoutListAdapter adapter;

	@SuppressWarnings("unused")
	public SoundLayoutList(Context context)
	{
		super(context);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundLayoutList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundLayoutList(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init(context);
	}

	private void init(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_sound_layout_list, this, true); // set layout height to wrap content
	}

	public void setAdapter(SoundLayoutList SoundLayoutListAdapter)
	{
		this.adapter = adapter;
		this.adapter.setOnItemClickListener(this);
		//this.soundSheets.setAdapter(adapter);
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		// TODO
	}

	@Override
	protected int getItemCount()
	{
		// TODO
		return 0;
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_sound_layouts;
	}

	@Override
	public void onItemClick(View view, String data, int position)
	{
		// TODO
	}

	public void toggleVisibility()
	{
		if (this.getVisibility() == View.VISIBLE)
			this.hideSelectSoundLayout();
		else
			this.showSelectSoundLayoutOverlay();
	}

	private void showSelectSoundLayoutOverlay()
	{
		this.setScaleY(0);
		this.setVisibility(View.VISIBLE);
		this.animate()
				.scaleY(1)
				.setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
				.setInterpolator(this.animationInterpolator)
				.start();
	}

	private void hideSelectSoundLayout()
	{
		this.animate()
				.scaleY(0)
				.setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
				.setInterpolator(this.animationInterpolator)
				.withEndAction(new Runnable() {
					@Override
					public void run() {
						SoundLayoutList.this.setVisibility(View.GONE);
					}
				})
				.start();
	}

}
