package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.customview.NavigationDrawerList;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;

import java.util.ArrayList;
import java.util.List;


public class SoundSheets
		extends
			NavigationDrawerList
		implements
			SoundSheetAdapter.OnItemClickListener
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
		playlist.setAdapter(this.adapter);
	}

	public void onActivityCreated(NavigationDrawerFragment parent)
	{
		super.parent = parent;
		this.notifyDataSetChanged(true);
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		List<SoundSheet> soundSheetsToRemove = new ArrayList<SoundSheet>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++) {
			int index = selectedItems.keyAt(i);
			soundSheetsToRemove.add(this.adapter.getValues().get(index));
		}

		this.adapter.removeAll(soundSheetsToRemove);

		SoundSheetManagerFragment soundSheetManagerfragment = (SoundSheetManagerFragment)this.parent.getFragmentManager()
				.findFragmentByTag(SoundSheetManagerFragment.TAG);

		SoundManagerFragment soundManagerFragment = (SoundManagerFragment) this.parent.getFragmentManager()
				.findFragmentByTag(SoundManagerFragment.TAG);

		BaseActivity activity = (BaseActivity)this.parent.getActivity();

		for (SoundSheet soundSheet: soundSheetsToRemove)
		{
			soundSheetManagerfragment.remove(soundSheet, false);
			soundManagerFragment.remove(soundSheet.getFragmentTag());
			activity.removeSoundFragment(soundSheet);

			if (soundSheet.getIsSelected())
			{
				List<SoundSheet> remainingSoundSheets = this.adapter.getValues();
				if (remainingSoundSheets.size() > 0)
				{
					this.adapter.setSelectedItem(0);
					this.parent.openSoundSheetFragment(remainingSoundSheets.get(0));
				}
			}
		}
		this.adapter.notifyDataSetChanged();
	}

	@Override
	protected int getItemCount() {
		return this.adapter.getItemCount();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else if (this.parent != null)
		{
			this.adapter.setSelectedItem(position);
			this.parent.openSoundSheetFragment(data);
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

