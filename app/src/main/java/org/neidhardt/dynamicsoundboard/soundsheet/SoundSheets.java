package org.neidhardt.dynamicsoundboard.soundsheet;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import org.neidhardt.dynamicsoundboard.customview.navigationdrawer.NavigationDrawerList;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;


public class SoundSheets
		extends
			NavigationDrawerList
		implements
			SoundSheetsAdapter.OnItemClickListener
{
	private RecyclerView soundSheets;
	private SoundSheetsAdapter adapter;

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
		this.soundSheets = (RecyclerView)this.findViewById(R.id.rv_sound_sheets);
		this.soundSheets.addItemDecoration(new DividerItemDecoration());
		this.soundSheets.setLayoutManager(new LinearLayoutManager(context));
		this.soundSheets.setItemAnimator(new DefaultItemAnimator());
	}

	public void setAdapter(SoundSheetsAdapter adapter)
	{
		this.adapter = adapter;
		this.adapter.setOnItemClickListener(this);
		this.soundSheets.setAdapter(adapter);
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_sound_sheets;
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		List<SoundSheet> soundSheetsToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			soundSheetsToRemove.add(this.adapter.getValues().get(index));
		}

		BaseActivity activity = (BaseActivity)this.parent.getActivity();
		SoundSheetsManagerFragment soundSheetsManagerfragment = this.parent.getSoundSheetManagerFragment();
		ServiceManagerFragment soundManagerFragment = this.parent.getServiceManagerFragment();
		MusicService service = soundManagerFragment.getSoundService();

		for (SoundSheet soundSheet: soundSheetsToRemove)
		{
			List<EnhancedMediaPlayer> soundsInSoundSheet = soundManagerFragment.getSounds().get(soundSheet.getFragmentTag());

			soundSheetsManagerfragment.remove(soundSheet, false);
			service.removeSounds(soundsInSoundSheet);
			activity.removeSoundFragment(soundSheet);

			if (soundSheet.getIsSelected())
			{
				List<SoundSheet> remainingSoundSheets = this.adapter.getValues();
				if (remainingSoundSheets.size() > 0)
				{
					this.adapter.setSelectedItem(0);
					this.parent.getBaseActivity().openSoundFragment(remainingSoundSheets.get(0));
				}
			}
		}
		soundManagerFragment.notifyPlaylist();
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
			this.parent.getBaseActivity().openSoundFragment(data);
		}
	}
}

