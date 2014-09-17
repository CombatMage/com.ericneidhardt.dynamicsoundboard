package com.ericneidhardt.dynamicsoundboard.playlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>
{
	private List<EnhancedMediaPlayer> playlist;

	public PlaylistAdapter()
	{
		this.playlist = new ArrayList<EnhancedMediaPlayer>();
	}

	public void clear()
	{
		this.playlist.clear();
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		this.playlist.addAll(mediaPlayers);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_playlist_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int i)
	{
		MediaPlayerData data = this.playlist.get(i).getMediaPlayerData();
		holder.label.setText(data.getLabel());
	}

	@Override
	public int getItemCount()
	{
		return this.playlist.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		private TextView label;
		private ImageView selectionIndicator;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
		}
	}
}
