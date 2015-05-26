package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerList;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.views.DividerItemDecoration;


public class SoundSheets
		extends
			NavigationDrawerList
		implements
			SoundSheetsAdapter.OnItemClickListener
{
	private SoundSheetsPresenter presenter;
	private SoundSheetsAdapter adapter;

	@SuppressWarnings("unused")
	public SoundSheets(Context context)
	{
		super(context);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init(context);
	}

	private void init(Context context)
	{
		this.presenter = new SoundSheetsPresenter();

		this.adapter = new SoundSheetsAdapter();
		this.adapter.setOnItemClickListener(this);

		LayoutInflater.from(context).inflate(R.layout.view_sound_sheets, this, true);

		RecyclerView soundSheets = (RecyclerView) this.findViewById(R.id.rv_sound_sheets);
		if (!this.isInEditMode())
		{
			soundSheets.setItemAnimator(new DefaultItemAnimator());
			soundSheets.setLayoutManager(new LinearLayoutManager(context));
			soundSheets.addItemDecoration(new DividerItemDecoration());
		}
		soundSheets.setAdapter(this.adapter);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.adapter.onAttachedToWindow();
		this.presenter.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		EventBus.getDefault().unregister(this.adapter);
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

	public SoundSheetsAdapter getAdapter()
	{
		return this.adapter;
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_sound_sheets;
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		this.presenter.onDeleteSelected(selectedItems);
	}

	@Override
	protected int getItemCount()
	{
		return this.adapter.getItemCount();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		this.presenter.onItemClick(view, data, position);
	}

	@Override
	public NavigationDrawerListPresenter getPresenter()
	{
		return this.presenter;
	}
}

