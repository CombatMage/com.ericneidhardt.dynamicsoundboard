package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.ListAdapter;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundProgressAdapter;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundProgressViewHolder;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnPlaylistChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetsChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsChangedEvent;

import java.util.List;

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
public class PlaylistAdapter
		extends
			SoundProgressAdapter<PlaylistAdapter.ViewHolder>
		implements
			OnPlaylistChangedEventListener,
			MediaPlayerEventListener,
			OnSoundSheetsChangedEventListener
{
	private PlaylistPresenter presenter;
	private Integer currentItemIndex;
	private OnItemClickListener onItemClickListener;

	public PlaylistAdapter(PlaylistPresenter presenter)
	{
		this.presenter = presenter;
	}

	@Override
	public void onAttachedToWindow()
	{
		EventBus bus = EventBus.getDefault();
		if (!bus.isRegistered(this))
			bus.register(this);

		this.notifyDataSetChanged();
		this.startProgressUpdateTimer();
	}

	@Override
	public void onDetachedFromWindow()
	{
		EventBus.getDefault().unregister(this);
		this.stopProgressUpdateTimer();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	@Override
	public List<EnhancedMediaPlayer> getValues()
	{
		return this.presenter.getPlaylist();
	}

	private EnhancedMediaPlayer getItem(int position)
	{
		return this.getValues().get(position);
	}

	public void startOrStopPlayList(EnhancedMediaPlayer nextActivePlayer)
	{
		List<EnhancedMediaPlayer> sounds = this.getValues();
		if (!this.getValues().contains(nextActivePlayer))
			throw new IllegalStateException("next active player " + nextActivePlayer + " is not in playlist");

		this.currentItemIndex = sounds.indexOf(nextActivePlayer);
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
		this.notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return R.layout.view_playlist_item;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		holder.bindData(this.getValues().get(position));
	}

	@Override
	public int getItemCount()
	{
		List<EnhancedMediaPlayer> players = this.getValues();
		return players != null ? this.getValues().size() : 0;
	}

	@Override
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		MediaPlayerData finishedPlayerData = event.getPlayer().getMediaPlayerData();
		if (this.currentItemIndex == null)
			return;
		MediaPlayerData currentPlayer = this.getItem(this.currentItemIndex).getMediaPlayerData();
		if (currentPlayer != finishedPlayerData)
			return;

		this.currentItemIndex++;
		if (this.currentItemIndex >= this.getItemCount())
			this.currentItemIndex = 0;

		this.getItem(this.currentItemIndex).playSound();
		this.notifyDataSetChanged();
	}

	@Override
	public void onEvent(SoundSheetsChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	/**
	 * This is called by greenRobot EventBus in case a sound sheet was removed. Retrieve new data, because this soundsheet might contains
	 * playlist entries.
	 * @param event delivered SoundSheetsRemovedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(SoundSheetsRemovedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEventMainThread(PlaylistChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	public Integer getCurrentItemIndex()
	{
		return currentItemIndex;
	}

	public void setCurrentItemIndex(Integer currentItemIndex)
	{
		this.currentItemIndex = currentItemIndex;
	}

	public class ViewHolder
			extends
				RecyclerView.ViewHolder
			implements
				View.OnClickListener,
				SoundProgressViewHolder
	{
		private View itemView;
		private TextView label;
		private ImageView selectionIndicator;
		private SeekBar timePosition;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.itemView = itemView;
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_progress);
			itemView.setOnClickListener(this);
		}

		protected void bindData(EnhancedMediaPlayer player)
		{
			this.timePosition.setMax(player.getDuration());
			this.label.setText(player.getMediaPlayerData().getLabel());
			this.selectionIndicator.setVisibility(player.isPlaying() ? View.VISIBLE : View.INVISIBLE);

			this.label.setActivated(player.getMediaPlayerData().isSelectedForDeletion());
			this.itemView.setSelected(player.getMediaPlayerData().isSelectedForDeletion());

			this.onProgressUpdate();
		}

		@Override
		public void onProgressUpdate()
		{
			EnhancedMediaPlayer player = getItem(this.getLayoutPosition());
			if (player != null && player.isPlaying())
			{
				timePosition.setProgress(player.getCurrentPosition());
				timePosition.setVisibility(View.VISIBLE);
			}
			else
				timePosition.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getLayoutPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, getItem(position), position);
		}
	}

	public interface OnItemClickListener
	{
		void onItemClick(View view, EnhancedMediaPlayer player, int position);
	}

}
