package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.DismissibleItemViewHolder;
import org.neidhardt.dynamicsoundboard.customview.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.SoundProgressAdapter;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;


public class SoundAdapter
		extends
			SoundProgressAdapter<SoundAdapter.ViewHolder>
{
	private static final String TAG = SoundAdapter.class.getName();
	private static final int VIEWPAGER_INDEX_SOUND_CONTROLS = 1;

	private final String parentFragmentTag;
	private final int heightListItem;
	private final int heightShadow;

	private OnItemDeleteListener onItemDeleteListener;

	public SoundAdapter(SoundSheetFragment parent)
	{
		this.parentFragmentTag = parent.getFragmentTag();
		this.heightListItem = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_xlarge);
		this.heightShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_shadow);
	}

	/**
	 * This is called by greenDao EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		String playerId = event.getPlayerId();
		List<EnhancedMediaPlayer> players = this.getValues();
		int count = players.size();
		for (int i = 0; i < count; i++)
		{
			if (players.get(i).getMediaPlayerData().getPlayerId().equals(playerId))
				this.notifyItemChanged(i);
		}
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	private EnhancedMediaPlayer getItem(int position)
	{
		List<EnhancedMediaPlayer> players = this.getValues();
		if (position > players.size())
			return null;
		return players.get(position);
	}

	@Override
	protected List<EnhancedMediaPlayer> getValues()
	{
		List<EnhancedMediaPlayer> sounds = super.serviceManagerFragment.getSounds().get(this.parentFragmentTag);
		if (sounds == null)
			return new ArrayList<>();
		return sounds;
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sound_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		holder.bindData(position);
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

		@Override
		protected void bindData(int positionInDataSet)
		{
			super.bindData(positionInDataSet);

			EnhancedMediaPlayer player = getItem(positionInDataSet);
			if (player == null)
				return;

			MediaPlayerData data = player.getMediaPlayerData();
			super.resetViewPager();

			if (!this.name.hasFocus())
				this.name.setText(data.getLabel());

			boolean isPlaying = player.isPlaying();
			this.play.setSelected(isPlaying);
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
			ViewGroup.LayoutParams params = container.getLayoutParams();
			params.height = heightListItem + heightShadow;
			this.container.setLayoutParams(params);
		}

		private void disableShadow()
		{
			this.shadowBottom.setVisibility(View.GONE);
			ViewGroup.LayoutParams params = container.getLayoutParams();
			params.height = heightListItem;
			this.container.setLayoutParams(params);
		}

		@Override
		protected Handler getHandler()
		{
			return handler;
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
		protected void delete()
		{
			int position = this.getPosition();
			EnhancedMediaPlayer player = getItem(position);
			if (player != null && onItemDeleteListener != null)
				onItemDeleteListener.onItemDelete(player, position);

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
			EnhancedMediaPlayer player = getItem(this.getPosition());
			if (player != null)
				player.getMediaPlayerData().setLabel(text);
		}

		@Override
		public void onClick(View view)
		{
			super.onClick(view);

			EnhancedMediaPlayer player = getItem(this.getPosition());
			if (player == null)
				return;
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
					ServiceManagerFragment fragment = serviceManagerFragment;
					fragment.getSoundService().toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), !isSelected);
					fragment.notifyPlaylist();
					break;
				case R.id.b_play:
					name.clearFocus();
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
				EnhancedMediaPlayer player = getItem(this.getPosition());
				if (player != null)
				player.setPositionTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			Logger.d(TAG, "onStartTrackingTouch");
			stopProgressUpdateTimer();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			Logger.d(TAG, "onStopTrackingTouch");
			startProgressUpdateTimer();
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
