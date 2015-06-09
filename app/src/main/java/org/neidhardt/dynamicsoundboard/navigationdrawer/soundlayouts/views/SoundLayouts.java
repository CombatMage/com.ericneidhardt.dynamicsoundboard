package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayouts extends NavigationDrawerList implements SoundLayoutsAdapter.OnItemClickListener
{
	private SoundLayoutsAdapter adapter;
	private SoundLayoutsPresenter presenter;

	@SuppressWarnings("unused")
	public SoundLayouts(Context context)
	{
		super(context);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundLayouts(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundLayouts(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init(context);
	}

	private void init(Context context)
	{
		this.adapter = new SoundLayoutsAdapter();
		this.adapter.setOnItemClickListener(this);

		this.presenter = new SoundLayoutsPresenter(SoundLayoutsManager.getInstance(), this.adapter);

		LayoutInflater.from(context).inflate(R.layout.view_sound_layout_list, this, true);

		RecyclerView soundLayouts = (RecyclerView) this.findViewById(R.id.rv_sound_layouts_list);
		if (!this.isInEditMode())
		{
			soundLayouts.addItemDecoration(new DividerItemDecoration());
			soundLayouts.setLayoutManager(new LinearLayoutManager(context));
			soundLayouts.setItemAnimator(new DefaultItemAnimator());
		}
		soundLayouts.setAdapter(this.adapter);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.presenter.onAttachedToWindow();
		this.adapter.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		this.adapter.onDetachedFromWindow();
		this.presenter.onDetachedFromWindow();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.presenter.setView(this);
	}

	@Override
	protected void onDeleteSelected()
	{
		this.presenter.onDeleteSelected();
	}

	@Override
	protected int getItemCount()
	{
		return this.adapter.getItemCount();
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_sound_layouts;
	}

	@Override
	public void onItemClick(View view, SoundLayout data, int position)
	{
		this.presenter.onItemClick(view, data, position);
	}

	@Override
	public void onItemSettingsClicked(SoundLayout data)
	{
		this.presenter.onItemSettingsClicked(data);
	}

	public boolean isActive()
	{
		return this.getVisibility() == View.VISIBLE;
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
		this.setVisibility(VISIBLE);
	}

	private void hideSelectSoundLayout()
	{
		this.setVisibility(INVISIBLE);
	}

	public SoundLayoutsAdapter getAdapter()
	{
		return this.adapter;
	}

	@Override
	public NavigationDrawerListPresenter getPresenter()
	{
		return this.presenter;
	}
}
