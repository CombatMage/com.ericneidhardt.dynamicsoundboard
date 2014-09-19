package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SoundAdapter
		extends
			RecyclerView.Adapter<SoundAdapter.ViewHolder>
		implements
			MediaPlayer.OnCompletionListener
{
	private static final int UPDATE_INTERVAL = 500;

	private Fragment parent;
	private List<EnhancedMediaPlayer> mediaPlayers;
	private Timer progressBarUpdateTimer;

	public SoundAdapter(Fragment parent)
	{
		this.parent = parent;
		this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (mediaPlayers == null)
			return;

		for (EnhancedMediaPlayer player : mediaPlayers)
			player.setOnCompletionListener(this);

		this.mediaPlayers.addAll(mediaPlayers);
	}

	public void clear()
	{
		this.mediaPlayers.clear();
	}

	@Override
	public int getItemCount()
	{
		return this.mediaPlayers.size();
	}

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	public void startProgressUpdateTimer()
	{
		TimerTask updateTimePositions = new TimerTask()
		{
			@Override
			public void run()
			{
				parent.getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		};
		this.progressBarUpdateTimer = new Timer();
		progressBarUpdateTimer.schedule(updateTimePositions, 0, UPDATE_INTERVAL);
	}

	public void stopProgressUpdateTimer()
	{
		this.progressBarUpdateTimer.cancel();
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
				SeekBar.OnSeekBarChangeListener,
				ViewPager.OnPageChangeListener
	{
		private CustomEditText name;
		private CheckBox play;
		private CheckBox loop;
		private CheckBox inPlaylist;
		private View stop;
		private SeekBar timePosition;

		public ViewHolder(View itemView)
		{
			super(itemView);

			ViewPager viewPager = (ViewPager)itemView;
			viewPager.setAdapter(new SoundItemPagerAdapter());
			viewPager.setOnPageChangeListener(this);

			this.name = (CustomEditText)itemView.findViewById(R.id.et_name_file);
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
		public void onPageScrolled(int i, float v, int i1)
		{
			stopProgressUpdateTimer();
		}

		@Override
		public void onPageScrollStateChanged(int i)
		{
			stopProgressUpdateTimer();
		}


		@Override
		public void onPageSelected(int i)
		{
			Timer delay = new Timer(); // delay restart of update timer cause page is selected before scrolling has settled
			delay.schedule(new TimerTask() {
				@Override
				public void run() {
					startProgressUpdateTimer();
				}
			}, UPDATE_INTERVAL);
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
					SoundManagerFragment fragment = (SoundManagerFragment)parent.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
					fragment.notifyPlayListChanged(player.getMediaPlayerData());
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
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			stopProgressUpdateTimer();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			startProgressUpdateTimer();
		}
	}

	private class SoundItemPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			switch (position)
			{
				case 0:
					return container.findViewById(R.id.layout_sound_controls);
				case 1:
					return container.findViewById(R.id.layout_remove_sound_item);
				default:
					throw new NullPointerException("instantiateItem: no view for position " + position + " is available");
			}
		}
	}
}
