package com.ericneidhardt.dynamicsoundboard.playlist;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.SoundProgressAdapter;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetFragment;

import java.util.List;

public class PlaylistAdapter
		extends
			SoundProgressAdapter<PlaylistAdapter.ViewHolder>
		implements
			MediaPlayer.OnCompletionListener
{

	private Fragment parent;
	private Integer currentItemIndex;
	private OnItemClickListener onItemClickListener;

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setParent(Fragment parent)
	{
		this.parent = parent;
	}

	public void clear()
	{
		super.sounds.clear();
	}

	public List<EnhancedMediaPlayer> getValues()
	{
		return super.sounds;
	}

	public void removeAll(List<EnhancedMediaPlayer> playerToRemove)
	{
		super.sounds.removeAll(playerToRemove);
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (mediaPlayers == null)
			return;

		for (EnhancedMediaPlayer player : mediaPlayers)
			player.setOnCompletionListener(this);

		super.sounds.addAll(mediaPlayers);
	}

	public void startPlayList(EnhancedMediaPlayer nextActivePlayer, int position)
	{
		for (EnhancedMediaPlayer player : super.sounds)
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
		EnhancedMediaPlayer player = super.sounds.get(i);
		holder.label.setText(player.getMediaPlayerData().getLabel());
		holder.selectionIndicator.setVisibility(player.isPlaying() ? View.VISIBLE : View.INVISIBLE);
		holder.updateProgress();
		if (player.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG))
			holder.isInPlayList.setVisibility(View.GONE);
		else
			holder.isInPlayList.setVisibility(View.VISIBLE);
	}

	@Override
	public int getItemCount()
	{
		return super.sounds.size();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		this.currentItemIndex++;
		if (this.currentItemIndex >= super.sounds.size())
			this.currentItemIndex = 0;
		super.sounds.get(this.currentItemIndex).playSound();
		this.notifyDataSetChanged();
	}

	public class ViewHolder
			extends
				RecyclerView.ViewHolder
			implements
				View.OnClickListener,
				CompoundButton.OnCheckedChangeListener
	{
		private TextView label;
		private ImageView selectionIndicator;
		private SeekBar progress;
		private CheckBox isInPlayList;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.progress = (SeekBar)itemView.findViewById(R.id.sb_progress);
			this.isInPlayList = (CheckBox)itemView.findViewById(R.id.cb_is_in_playlist);
			this.isInPlayList.setChecked(true);
			this.isInPlayList.setOnCheckedChangeListener(this);
			itemView.setOnClickListener(this);
		}

		private void updateProgress()
		{
			EnhancedMediaPlayer player = sounds.get(this.getPosition());
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
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (!isChecked)
			{
				EnhancedMediaPlayer player = sounds.get(this.getPosition());

				SoundSheetFragment fragment = (SoundSheetFragment)parent.getFragmentManager()
						.findFragmentByTag(player.getMediaPlayerData().getFragmentTag());

				sounds.remove(player);
				player.getMediaPlayerData().setIsInPlaylist(false);
				player.destroy();

				if (fragment != null)
					fragment.notifyDataSetChanged(false);

				notifyDataSetChanged();
			}
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, sounds.get(position), position);
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, EnhancedMediaPlayer player, int position);
	}
}
