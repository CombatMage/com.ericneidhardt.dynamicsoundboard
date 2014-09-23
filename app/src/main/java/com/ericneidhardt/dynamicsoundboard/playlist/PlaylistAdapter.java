package com.ericneidhardt.dynamicsoundboard.playlist;

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter
		extends
			RecyclerView.Adapter<PlaylistAdapter.ViewHolder>
		implements
			MediaPlayer.OnCompletionListener
{
	private List<EnhancedMediaPlayer> playlist;
	private Integer currentItemIndex;
	private OnItemClickListener onItemClickListener;

	public PlaylistAdapter()
	{
		this.playlist = new ArrayList<EnhancedMediaPlayer>();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void clear()
	{
		this.playlist.clear();
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (mediaPlayers == null)
			return;

		for (EnhancedMediaPlayer player : mediaPlayers)
			player.setOnCompletionListener(this);

		this.playlist.addAll(mediaPlayers);
	}

	public void startPlayList(EnhancedMediaPlayer nextActivePlayer, int position)
	{
		for (EnhancedMediaPlayer player : this.playlist)
		{
			if (player.equals(nextActivePlayer))
				continue;
			player.stopSound();
		}

		if (nextActivePlayer.isPlaying())
			nextActivePlayer.pauseSound();
		else
			nextActivePlayer.playSound();
		this.currentItemIndex = position;
		this.notifyDataSetChanged();
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
		EnhancedMediaPlayer player = this.playlist.get(i);
		holder.label.setText(player.getMediaPlayerData().getLabel());
		holder.selectionIndicator.setVisibility(player.isPlaying() ? View.VISIBLE : View.GONE);
	}

	@Override
	public int getItemCount()
	{
		return this.playlist.size();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		this.currentItemIndex++;
		if (this.currentItemIndex >= this.playlist.size())
			this.currentItemIndex = 0;
		this.playlist.get(this.currentItemIndex).playSound();
		this.notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private TextView label;
		private ImageView selectionIndicator;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, playlist.get(position), position);
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, EnhancedMediaPlayer player, int position);
	}
}
