package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.CustomEditText;
import org.neidhardt.dynamicsoundboard.customview.DismissibleItemViewHolder;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.SoundProgressAdapter;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;


public class SoundAdapter
		extends
			SoundProgressAdapter<SoundAdapter.ViewHolder>
		implements
			EnhancedMediaPlayer.OnMediaPlayerStateChangedListener
{
	private static final int VIEWPAGER_INDEX_SOUND_CONTROLS = 1;

	private SoundSheetFragment parent;
	private final String parentFragmentTag;

	private OnItemDeleteListener onItemDeleteListener;

	public SoundAdapter(SoundSheetFragment parent)
	{
		this.parent = parent;
		this.parentFragmentTag = parent.getFragmentTag();
	}

	public void onParentResume(SoundSheetFragment parent)
	{
		this.parent = parent;

		super.setServiceManagerFragment(this.parent.getServiceManagerFragment());
		super.scheduleProgressUpdateTimer();
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	private EnhancedMediaPlayer getItem(int position)
	{
		return this.getValues().get(position);
	}

	@Override
	protected List<EnhancedMediaPlayer> getValues()
	{
		List<EnhancedMediaPlayer> sounds = super.serviceManagerFragment.getSounds().get(this.parentFragmentTag);
		if (sounds == null)
			return new ArrayList<>();

		for (EnhancedMediaPlayer sound : sounds)
			sound.addOnMediaPlayerStateChangedListener(this);

		return sounds;
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
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
	public void onMediaPlayerStateChanged(MediaPlayer player, boolean hasPlayerCompleted)
	{
		if (player instanceof EnhancedMediaPlayer)
			this.notifyItemChanged(this.getValues().indexOf(player));
	}

	public class ViewHolder
			extends
				DismissibleItemViewHolder
			implements
				View.OnClickListener,
				CustomEditText.OnTextEditedListener,
				SeekBar.OnSeekBarChangeListener
	{
		private final View container;
		private final CustomEditText name;
		private final View play;
		private final View loop;
		private final View inPlaylist;
		private final View stop;
		private final View fadeOut;
		private final SeekBar timePosition;

		private final View shadowBottomDeleteViewLeft;
		private final View shadowBottomDeleteViewRight;
		private final View shadowBottom;

		public ViewHolder(View itemView)
		{
			super(itemView);

			this.container = itemView;

			this.name = (CustomEditText)itemView.findViewById(R.id.et_name_file);
			this.play = itemView.findViewById(R.id.b_play);
			this.loop = itemView.findViewById(R.id.b_loop);
			this.inPlaylist = itemView.findViewById(R.id.b_add_to_playlist);
			this.stop = itemView.findViewById(R.id.b_stop);
			this.fadeOut = itemView.findViewById(R.id.b_fade_out);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_progress);

			this.name.setOnTextEditedListener(this);
			this.play.setOnClickListener(this);
			this.loop.setOnClickListener(this);
			this.inPlaylist.setOnClickListener(this);
			this.stop.setOnClickListener(this);
			this.fadeOut.setOnClickListener(this);
			this.timePosition.setOnSeekBarChangeListener(this);

			this.shadowBottomDeleteViewLeft = itemView.findViewById(R.id.v_shadow_bottom_left);
			this.shadowBottomDeleteViewRight = itemView.findViewById(R.id.v_shadow_bottom_right);
			this.shadowBottom = itemView.findViewById(R.id.v_shadow_bottom);
		}

		private void bindData(int positionInDataSet)
		{
			EnhancedMediaPlayer player = getItem(positionInDataSet);
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
			int heightWithShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_xlarge)
					+ parent.getResources().getDimensionPixelSize(R.dimen.height_shadow);

			ViewGroup.LayoutParams params = container.getLayoutParams();
			params.height = heightWithShadow;
			this.container.setLayoutParams(params);
		}

		private void disableShadow()
		{
			this.shadowBottom.setVisibility(View.GONE);
			int heightWithOutShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_xlarge);

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
				scheduleProgressUpdateTimer();
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
						onItemDeleteListener.onItemDelete(getItem(position), position);
				}
			}, UPDATE_INTERVAL);

			handler.postDelayed(new Runnable()
			{ // delay restart of update timer, to allow deletion animation to settle
				@Override
				public void run()
				{
					scheduleProgressUpdateTimer();
				}
			}, 2 * UPDATE_INTERVAL);
		}

		@Override
		public void onTextEdited(String text)
		{
			getItem(this.getPosition()).getMediaPlayerData().setLabel(text);
		}

		@Override
		public void onClick(View view)
		{
			EnhancedMediaPlayer player = getItem(this.getPosition());
			boolean isSelected = view.isSelected();
			int id = view.getId();
			switch (id)
			{
				case R.id.b_stop:
					player.stopSound();
					break;
				case R.id.b_fade_out:
					player.fadeOutSound();
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
						scheduleProgressUpdateTimer();
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
				EnhancedMediaPlayer player = getItem(this.getPosition());
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
			scheduleProgressUpdateTimer();
		}
	}

	private class SoundItemPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return 3; // main sound control + delete left and right
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
