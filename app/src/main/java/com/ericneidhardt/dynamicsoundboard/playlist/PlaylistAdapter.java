package com.ericneidhardt.dynamicsoundboard.playlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>
{
	public PlaylistAdapter()
	{

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		// TODO inflate real layout
		//View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_playlist_item, parent, false);
		//return new ViewHolder(view);
		return new ViewHolder(new TextView(parent.getContext()));
	}

	@Override
	public void onBindViewHolder(ViewHolder parent, int i)
	{
		// TODO bind data
	}

	@Override
	public int getItemCount() {
		return 0;
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public ViewHolder(View itemView)
		{
			super(itemView);
		}
	}
}
