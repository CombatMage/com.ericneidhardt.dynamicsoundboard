package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.NavigationDrawerList;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;

import java.util.List;


public class SoundSheets
		extends
		NavigationDrawerList
		implements
			SoundSheetAdapter.OnItemClickListener,
			SoundSheetAdapter.OnItemDeleteListener
{
	private SoundSheetAdapter adapter;

	@SuppressWarnings("unused")
	public SoundSheets(Context context)
	{
		super(context);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	private void inflateLayout(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_sound_sheets, this, true);
	}

	private void initRecycleView(Context context)
	{
		RecyclerView playlist = (RecyclerView)this.findViewById(R.id.rv_sound_sheets);
		playlist.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST, null));
		playlist.setLayoutManager(new LinearLayoutManager(context));
		playlist.setItemAnimator(new DefaultItemAnimator());

		this.adapter = new SoundSheetAdapter();
		this.adapter.setOnItemClickListener(this);
		this.adapter.setOnItemDeleteListener(this);
		playlist.setAdapter(this.adapter);
	}

	public void onActivityCreated(NavigationDrawerFragment parent)
	{
		super.parent = parent;
		this.notifyDataSetChanged(true);
	}

	public void prepareItemDeletion()
	{
		super.prepareItemDeletion();
		// TODO
	}

	@Override
	protected int getItemCount() {
		return this.adapter.getItemCount();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.parent != null)
		{
			this.adapter.setSelectedItem(position);
			this.parent.openSoundSheetFragment(data);
		}
	}

	@Override
	public void onItemDelete(View view, SoundSheet data, int position)
	{
		this.adapter.remove(data);
		this.adapter.notifyItemRemoved(position);

		SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.parent.getFragmentManager()
				.findFragmentByTag(SoundSheetManagerFragment.TAG);

		fragment.remove(data, false);

		if (this.parent != null)
		{
			((BaseActivity)this.parent.getActivity()).removeSoundFragment(data);
			((SoundManagerFragment)this.parent.getFragmentManager()
					.findFragmentByTag(SoundManagerFragment.TAG)).remove(data.getFragmentTag());

			if (data.getIsSelected())
			{
				List<SoundSheet> soundSheets = this.adapter.getValues();
				if (soundSheets.size() > 0)
				{
					int positionOfNewSelectedSoundSheet = (position > 0) ? position - 1 : 0;
					this.adapter.setSelectedItem(positionOfNewSelectedSoundSheet);
					this.parent.openSoundSheetFragment(soundSheets.get(positionOfNewSelectedSoundSheet));
				}
			}
		}
	}

	public void notifyDataSetChanged(boolean newSoundAvailable)
	{
		if (newSoundAvailable)
		{
			SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.parent.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
			this.adapter.clear();
			this.adapter.addAll(fragment.getAll());
		}
		this.adapter.notifyDataSetChanged();
	}
}

