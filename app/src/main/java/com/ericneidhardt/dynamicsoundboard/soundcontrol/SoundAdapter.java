package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DialogEditText;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;


public class SoundAdapter
		extends
			RecyclerView.Adapter<SoundAdapter.ViewHolder>
		implements
			MediaPlayer.OnCompletionListener
{
	private List<EnhancedMediaPlayer> mediaPlayers;

	public SoundAdapter()
	{
		this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers, boolean notifyDataSetChanged)
	{
		if (mediaPlayers == null)
			return;

		for (EnhancedMediaPlayer player : mediaPlayers)
			player.setOnCompletionListener(this);

		this.mediaPlayers.addAll(mediaPlayers);
		if (notifyDataSetChanged)
			this.notifyDataSetChanged();
	}

	public void remove(EnhancedMediaPlayer mediaPlayer, boolean notifyDataSetChanged)
	{
		int position = this.mediaPlayers.indexOf(mediaPlayer);
		this.mediaPlayers.remove(position);
		if (notifyDataSetChanged)
			this.notifyItemRemoved(position);
	}

	public void clear(boolean notifyDataSetChanged)
	{
		this.mediaPlayers.clear();
		if (notifyDataSetChanged)
			this.notifyDataSetChanged();
	}

	@Override
	public int getItemCount()
	{
		return this.mediaPlayers.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sound_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		EnhancedMediaPlayer player = this.mediaPlayers.get(position);
		MediaPlayerData data = player.getMediaPlayerData();

		holder.name.setText(data.getLabel());
		holder.play.setChecked(player.isPlaying());
		holder.loop.setChecked(data.getIsLoop());
		holder.inPlaylist.setChecked(data.getIsInPlaylist());

		holder.timePosition.setMax(player.getDuration());
		holder.timePosition.setProgress(player.getCurrentPosition());
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		if (mp instanceof EnhancedMediaPlayer)
			this.notifyItemChanged(this.mediaPlayers.indexOf(mp));
	}

	public class ViewHolder
			extends
				RecyclerView.ViewHolder
			implements
				View.OnClickListener,
				CustomEditText.OnTextEditedListener,
				CompoundButton.OnCheckedChangeListener,
				SeekBar.OnSeekBarChangeListener
	{
		private DialogEditText name;
		private CheckBox play;
		private CheckBox loop;
		private CheckBox inPlaylist;
		private View stop;
		private SeekBar timePosition;

		public ViewHolder(View itemView)
		{
			super(itemView);

			this.name = (DialogEditText)itemView.findViewById(R.id.et_name_file);
			this.play = (CheckBox)itemView.findViewById(R.id.cb_play);
			this.loop = (CheckBox)itemView.findViewById(R.id.cb_loop);
			this.inPlaylist = (CheckBox)itemView.findViewById(R.id.cb_add_to_playlist);
			this.stop = itemView.findViewById(R.id.b_stop);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_time);

			this.name.setOnTextEditedListener(this);
			this.play.setOnCheckedChangeListener(this);
			this.loop.setOnCheckedChangeListener(this);
			this.inPlaylist.setOnCheckedChangeListener(this);
			this.stop.setOnClickListener(this);
			this.timePosition.setOnSeekBarChangeListener(this);
		}

		@Override
		public void onTextEdited(String text)
		{
			mediaPlayers.get(this.getPosition()).getMediaPlayerData().setLabel(text);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			EnhancedMediaPlayer player = mediaPlayers.get(this.getPosition());
			switch (buttonView.getId())
			{
				case R.id.cb_play:
					if (isChecked)
						player.playSound();
					else
						player.pauseSound();
					break;
				case R.id.cb_loop:
					player.setLooping(isChecked);
					break;
				case R.id.cb_add_to_playlist:
					player.setInPlaylist(isChecked);
					break;
			}
		}

		@Override
		public void onClick(View view)
		{
			EnhancedMediaPlayer player = mediaPlayers.get(this.getPosition());
			switch (view.getId())
			{
				case R.id.b_stop:
					player.stopSound();
					notifyItemChanged(getPosition());
					break;
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			if (fromUser)
			{
				EnhancedMediaPlayer player = mediaPlayers.get(this.getPosition());
				player.setPositionTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	}
}
