package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist;

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
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerList;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.PlaylistSoundsRemovedEvent;
import org.neidhardt.dynamicsoundboard.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
public class Playlist extends NavigationDrawerList implements PlaylistAdapter.OnItemClickListener
{
	public static final String TAG = Playlist.class.getName();

	private RecyclerView playlist;
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
		this.playlist = (RecyclerView)this.findViewById(R.id.rv_playlist);
		if (!this.isInEditMode())
		{
			this.playlist.addItemDecoration(new DividerItemDecoration());
			this.playlist.setLayoutManager(new LinearLayoutManager(context));
			this.playlist.setItemAnimator(new DefaultItemAnimator());
		}
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		EventBus bus = EventBus.getDefault();
		if (!bus.isRegistered(this.adapter))
			bus.register(this.adapter);

		this.adapter.notifyDataSetChanged();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		EventBus.getDefault().unregister(this.adapter);
		super.onDetachedFromWindow();
	}

	public void setAdapter(PlaylistAdapter adapter)
	{
		this.adapter = adapter;
		this.adapter.setOnItemClickListener(this);
		this.playlist.setAdapter(adapter);
		this.adapter.setRecyclerView(this.playlist);
	}

	@Override
	public void onItemClick(View view, EnhancedMediaPlayer player, int position)
	{
		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else
			this.adapter.startOrStopPlayList(player);
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_play_list_sounds;
	}

	@Override
	protected void onDeleteSelected(SparseArray<View> selectedItems)
	{
		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			playersToRemove.add(this.adapter.getValues().get(index));
		}

		EventBus.getDefault().post(new PlaylistSoundsRemovedEvent(playersToRemove));
		this.adapter.notifyDataSetChanged();
	}

	@Override
	protected int getItemCount() {
		return this.adapter.getItemCount();
	}

	public void notifyDataSetChanged()
	{
		this.adapter.notifyDataSetChanged();
	}

}
