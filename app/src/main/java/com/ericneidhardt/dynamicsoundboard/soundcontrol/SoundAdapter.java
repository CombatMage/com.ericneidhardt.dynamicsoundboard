package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DismissibleItemViewHolder;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.SoundProgressAdapter;
import com.ericneidhardt.dynamicsoundboard.storage.ServiceManagerFragment;

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

	public List<EnhancedMediaPlayer> getValues()
	{
		return this.sounds;
	}

	public void removeAll(List<EnhancedMediaPlayer> players)
	{
		if (players == null)
			return;
		this.sounds.removeAll(players);
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
		holder.bindData(position);
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
				SeekBar.OnSeekBarChangeListener
	{
		private View container;
		private CustomEditText name;
		private View play;
		private View loop;
		private View inPlaylist;
		private View stop;
		private SeekBar timePosition;

		private View shadowBottomDeleteViewLeft;
		private View shadowBottomDeleteViewRight;
		private View shadowBottom;

		public ViewHolder(View itemView)
		{
			super(itemView);

			this.container = itemView;

			this.name = (CustomEditText)itemView.findViewById(R.id.et_name_file);
			this.play = itemView.findViewById(R.id.b_play);
			this.loop = itemView.findViewById(R.id.b_loop);
			this.inPlaylist = itemView.findViewById(R.id.b_add_to_playlist);
			this.stop = itemView.findViewById(R.id.b_stop);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_progress);

			this.name.setOnTextEditedListener(this);
			this.play.setOnClickListener(this);
			this.loop.setOnClickListener(this);
			this.inPlaylist.setOnClickListener(this);
			this.stop.setOnClickListener(this);
			this.timePosition.setOnSeekBarChangeListener(this);

			this.shadowBottomDeleteViewLeft = itemView.findViewById(R.id.v_shadow_bottom_left);
			this.shadowBottomDeleteViewRight = itemView.findViewById(R.id.v_shadow_bottom_right);
			this.shadowBottom = itemView.findViewById(R.id.v_shadow_bottom);
		}

		private void bindData(int positionInDataSet)
		{
			EnhancedMediaPlayer player = sounds.get(positionInDataSet);
			MediaPlayerData data = player.getMediaPlayerData();

			super.resetViewPager();

			this.name.setText(data.getLabel());
			this.play.setSelected(player.isPlaying());
			this.loop.setSelected(data.getIsLoop());
			this.inPlaylist.setSelected(data.getIsInPlaylist());

			this.timePosition.setMax(player.getDuration());
			this.timePosition.setProgress(player.getCurrentPosition());

			boolean isLastElement = positionInDataSet == getItemCount() - 1;
			int shadowViewState = isLastElement  ? View.GONE : View.VISIBLE;
			this.shadowBottomDeleteViewLeft.setVisibility(shadowViewState);
			this.shadowBottomDeleteViewRight.setVisibility(shadowViewState);

			boolean isLastItem = positionInDataSet == getItemCount() - 1;

			if (isLastItem)
				this.enableShadowOnLastItem();
			else
				this.disableShadow();
		}

		private void enableShadowOnLastItem()
		{
			this.shadowBottom.setVisibility(View.VISIBLE);
			int heightWithShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_large)
					+ parent.getResources().getDimensionPixelSize(R.dimen.height_shadow);

			ViewGroup.LayoutParams params = container.getLayoutParams();
			params.height = heightWithShadow;
			this.container.setLayoutParams(params);
		}

		private void disableShadow()
		{
			this.shadowBottom.setVisibility(View.GONE);
			int heightWithOutShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_large);

			ViewGroup.LayoutParams params = container.getLayoutParams();
			params.height = heightWithOutShadow;
			this.container.setLayoutParams(params);
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
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{}

		@Override
		public void onPageScrollStateChanged(int state)
		{
			if (state == ViewPager.SCROLL_STATE_IDLE)
				startProgressUpdateTimer();
			else
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

			handler.postDelayed(new Runnable()
			{ // delay restart of update timer, to allow deletion animation to settle
				@Override
				public void run()
				{
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
		public void onClick(View view)
		{
			EnhancedMediaPlayer player = sounds.get(this.getPosition());
			int id = view.getId();
			boolean isSelected = view.isSelected();
			switch (id)
			{
				case R.id.b_stop:
					player.stopSound();
					break;
				case R.id.b_loop:
					view.setSelected(!isSelected);
					player.setLooping(!isSelected);
					break;
				case R.id.b_add_to_playlist:
					view.setSelected(!isSelected);
					player.setIsInPlaylist(!isSelected);
					ServiceManagerFragment fragment = parent.getServiceManagerFragment();
					fragment.getSoundService().toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), !isSelected);
					fragment.notifyPlaylist();
					break;
				case R.id.b_play:
					view.setSelected(!isSelected);
					if (!isSelected)
					{
						startProgressUpdateTimer();
						player.playSound();
					}
					else
					{
						player.pauseSound();
					}
					break;
			}
			notifyItemChanged(getPosition());
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
