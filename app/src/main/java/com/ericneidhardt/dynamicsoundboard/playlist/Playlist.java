package com.ericneidhardt.dynamicsoundboard.playlist;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.customview.NavigationDrawerList;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;

public class Playlist extends NavigationDrawerList implements PlaylistAdapter.OnItemClickListener
{
	public static final String TAG = Playlist.class.getSimpleName();

	private PlaylistAdapter adapter;

	@SuppressWarnings("unused")
	public Playlist(Context context)
	{
		super(context);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	@SuppressWarnings("unused")
	public Playlist(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	@SuppressWarnings("unused")
	public Playlist(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.inflateLayout(context);
		this.initRecycleView(context);
	}

	private void inflateLayout(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_playlist, this, true);
	}

	private void initRecycleView(Context context)
	{
		RecyclerView playlist = (RecyclerView)this.findViewById(R.id.rv_playlist);
		playlist.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST, null));
		playlist.setLayoutManager(new LinearLayoutManager(context));
		playlist.setItemAnimator(new DefaultItemAnimator());

		this.adapter = new PlaylistAdapter();
		this.adapter.setOnItemClickListener(this);
		playlist.setAdapter(this.adapter);
	}

	public void onActivityCreated(NavigationDrawerFragment parent)
	{
		super.parent = parent;
		this.notifyDataSetChanged(true);
	}

	@Override
	public void onItemClick(View view, EnhancedMediaPlayer player, int position)
	{
		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else if (this.parent != null)
			this.adapter.startPlayList(player, position);
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		// TODO
	}

	@Override
	protected int getItemCount() {
		return this.adapter.getItemCount();
	}

	public void notifyDataSetChanged(boolean newSoundAvailable)
	{
		if (newSoundAvailable)
		{
			SoundManagerFragment fragment = (SoundManagerFragment)this.parent.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
			this.adapter.clear();
			this.adapter.addAll(fragment.getPlayList());
		}
		this.adapter.notifyDataSetChanged();
	}

}
