package org.neidhardt.dynamicsoundboard.playlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.SoundProgressAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
public class PlaylistAdapter extends SoundProgressAdapter<PlaylistAdapter.ViewHolder>
{
	private Integer currentItemIndex;
	private OnItemClickListener onItemClickListener;

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	@Override
	protected List<EnhancedMediaPlayer> getValues()
	{
		if (super.serviceManagerFragment == null)
			return null;
		List<EnhancedMediaPlayer> sounds = super.serviceManagerFragment.getPlayList();
		if (sounds == null)
			sounds = new ArrayList<>();
		return sounds;
	}

	private EnhancedMediaPlayer getItem(int position)
	{
		return this.getValues().get(position);
	}

	public void startOrStopPlayList(EnhancedMediaPlayer nextActivePlayer, int position)
	{
		List<EnhancedMediaPlayer> sounds = super.serviceManagerFragment.getPlayList();
		for (EnhancedMediaPlayer player : sounds)
		{
			if (player.equals(nextActivePlayer))
				continue;
			player.stopSound();
		}

		if (nextActivePlayer.isPlaying())
		{
			this.stopProgressUpdateTimer();
			nextActivePlayer.pauseSound();
		}
		else
		{
			this.startProgressUpdateTimer();
			nextActivePlayer.playSound();
		}
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
		EnhancedMediaPlayer player = this.getItem(i);
		holder.label.setText(player.getMediaPlayerData().getLabel());
		holder.selectionIndicator.setVisibility(player.isPlaying() ? View.VISIBLE : View.INVISIBLE);
		holder.updateProgress();
	}

	@Override
	public int getItemCount()
	{
		List<EnhancedMediaPlayer> players = this.getValues();
		return players != null ? this.getValues().size() : 0;
	}

	/**
	 * This is called by greenDao EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	/**
	 * This is called by greenDao EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		this.currentItemIndex++;
		if (this.currentItemIndex >= this.getItemCount())
			this.currentItemIndex = 0;

		this.getItem(this.currentItemIndex).playSound();
		this.notifyDataSetChanged();
	}

	public class ViewHolder
			extends
				RecyclerView.ViewHolder
			implements
				View.OnClickListener
	{
		private TextView label;
		private ImageView selectionIndicator;
		private SeekBar progress;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.progress = (SeekBar)itemView.findViewById(R.id.sb_progress);
			itemView.setOnClickListener(this);
		}

		private void updateProgress()
		{
			EnhancedMediaPlayer player = getItem(this.getPosition());
			if (player.isPlaying())
			{
				progress.setMax(player.getDuration());
				progress.setProgress(player.getCurrentPosition());
				progress.setVisibility(View.VISIBLE);
			}
			else
				progress.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, getItem(position), position);
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, EnhancedMediaPlayer player, int position);
	}

}
