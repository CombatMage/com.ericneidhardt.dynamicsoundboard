package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DismissibleItemViewHolder;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.SoundProgressAdapter;

import java.util.List;


public class SoundAdapter
		extends
			SoundProgressAdapter<SoundAdapter.ViewHolder>
		implements
			MediaPlayer.OnCompletionListener
{
	private static final int VIEWPAGER_INDEX_SOUND_CONTROLS = 1;

	private SoundSheetFragment parent;

	private OnItemDeleteListener onItemDeleteListener;

	public SoundAdapter(SoundSheetFragment parent)
	{
		this.parent = parent;
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (mediaPlayers == null)
			return;

		for (EnhancedMediaPlayer player : mediaPlayers)
			player.setOnCompletionListener(this);

		this.sounds.addAll(mediaPlayers);
	}

	public void clear()
	{
		this.sounds.clear();
	}

	public void remove(int index)
	{
		this.sounds.remove(index);
	}

	@Override
	public int getItemCount()
	{
		return this.sounds.size();
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
		EnhancedMediaPlayer player = this.sounds.get(position);
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
			this.notifyItemChanged(this.sounds.indexOf(mp));
	}

	public class ViewHolder
			extends
				DismissibleItemViewHolder
			implements
				View.OnClickListener,
				CustomEditText.OnTextEditedListener,
				CompoundButton.OnCheckedChangeListener,
				SeekBar.OnSeekBarChangeListener
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

			this.name = (CustomEditText)itemView.findViewById(R.id.et_name_file);
			this.play = (CheckBox)itemView.findViewById(R.id.cb_play);
			this.loop = (CheckBox)itemView.findViewById(R.id.cb_loop);
			this.inPlaylist = (CheckBox)itemView.findViewById(R.id.cb_add_to_playlist);
			this.stop = itemView.findViewById(R.id.b_stop);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_progress);

			this.name.setOnTextEditedListener(this);
			this.play.setOnCheckedChangeListener(this);
			this.loop.setOnCheckedChangeListener(this);
			this.inPlaylist.setOnCheckedChangeListener(this);
			this.stop.setOnClickListener(this);
			this.timePosition.setOnSeekBarChangeListener(this);
		}

		@Override
		protected PagerAdapter getPagerAdapter()
		{
			return new SoundItemPagerAdapter();
		}

		@Override
		protected int getIndexOfContentPage()
		{
			return VIEWPAGER_INDEX_SOUND_CONTROLS;
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
		public void onPageSelected(final int selectedPage)
		{
			final int position = this.getPosition();
			handler.postDelayed(new Runnable() { // delay deletion, because page is selected before scrolling has settled
				@Override
				public void run() {
					if (selectedPage != VIEWPAGER_INDEX_SOUND_CONTROLS && onItemDeleteListener != null)
						onItemDeleteListener.onItemDelete(sounds.get(position), position);
				}
			}, UPDATE_INTERVAL);

			handler.postDelayed(new Runnable() { // delay restart of update timer, to allow deletion animation to settle
				@Override
				public void run() {
					startProgressUpdateTimer();
				}
			}, 2 * UPDATE_INTERVAL);
		}

		@Override
		public void onTextEdited(String text)
		{
			sounds.get(this.getPosition()).getMediaPlayerData().setLabel(text);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			EnhancedMediaPlayer player = sounds.get(this.getPosition());
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
					player.setIsInPlaylist(isChecked);
					SoundManagerFragment fragment = (SoundManagerFragment)parent.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
					fragment.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), isChecked);
					fragment.notifyPlaylist();
					notifyDataSetChanged();
					break;
			}
		}

		@Override
		public void onClick(View view)
		{
			EnhancedMediaPlayer player = sounds.get(this.getPosition());
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
				EnhancedMediaPlayer player = sounds.get(this.getPosition());
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
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			if (position == VIEWPAGER_INDEX_SOUND_CONTROLS)
				return container.findViewById(R.id.layout_sound_controls);
			else if (position == VIEWPAGER_INDEX_SOUND_CONTROLS - 1)
				return container.findViewById(R.id.layout_remove_sound_item_left);
			else
				return container.findViewById(R.id.layout_remove_sound_item_right);
		}
	}

	public static interface OnItemDeleteListener
	{
		public void onItemDelete(EnhancedMediaPlayer data, int position);
	}
}
