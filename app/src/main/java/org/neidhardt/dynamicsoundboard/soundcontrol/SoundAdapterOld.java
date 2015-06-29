package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DismissibleItemViewHolder;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText;

import java.util.List;


public class SoundAdapterOld
		extends
			SoundProgressAdapter<SoundAdapterOld.ViewHolder>
		implements
			MediaPlayerEventListener
{
	private static final String TAG = SoundAdapterOld.class.getName();
	private static final int VIEWPAGER_INDEX_SOUND_CONTROLS = 1;

	private final String parentFragmentTag;
	private final int heightListItem;
	private final int heightShadow;

	private OnItemDeleteListener onItemDeleteListener;
	private SoundsDataAccess soundsDataAccess;
	private SoundsDataStorage soundsDataStorage;

	private EventBus eventBus;

	public SoundAdapterOld(SoundSheetFragment parent, SoundsDataAccess soundsDataAccess, SoundsDataStorage soundsDataStorage)
	{
		this.eventBus = EventBus.getDefault();
		this.parentFragmentTag = parent.getFragmentTag();
		this.heightListItem = parent.getResources().getDimensionPixelSize(R.dimen.height_list_item_xlarge);
		this.heightShadow = parent.getResources().getDimensionPixelSize(R.dimen.height_shadow);
		this.soundsDataAccess = soundsDataAccess;
		this.soundsDataStorage = soundsDataStorage;
	}

	@Override
	public void onAttachedToWindow() {}

	@Override
	public void onDetachedFromWindow() {}

	@Override
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

	@Override
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		Logger.d(TAG, "onEvent :" +  event);
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	private EnhancedMediaPlayer getItem(int position)
	{
		List<EnhancedMediaPlayer> players = this.getValues();
		if (position >= players.size())
			return null;
		return players.get(position);
	}

	@Override
	public List<EnhancedMediaPlayer> getValues()
	{
		return this.soundsDataAccess.getSoundsInFragment(this.parentFragmentTag);
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
	}

	@Override
	public int getItemViewType(int position)
	{
		return R.layout.view_sound_item;
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
		holder.bindData(position);
	}

	public class ViewHolder
			extends
				DismissibleItemViewHolder
			implements
				View.OnClickListener,
				CustomEditText.OnTextEditedListener,
				SeekBar.OnSeekBarChangeListener,
				SoundProgressViewHolder
	{
		private final View container;
		private final CustomEditText name;
		private final View play;
		private final View loop;
		private final View inPlaylist;
		private final View stop;
		private final View settings;
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
			this.settings = itemView.findViewById(R.id.b_settings);
			this.timePosition = (SeekBar)itemView.findViewById(R.id.sb_progress);

			this.name.setOnTextEditedListener(this);
			this.play.setOnClickListener(this);
			this.loop.setOnClickListener(this);
			this.inPlaylist.setOnClickListener(this);
			this.stop.setOnClickListener(this);
			this.settings.setOnClickListener(this);
			this.timePosition.setOnSeekBarChangeListener(this);

			this.shadowBottomDeleteViewLeft = itemView.findViewById(R.id.v_shadow_bottom_left);
			this.shadowBottomDeleteViewRight = itemView.findViewById(R.id.v_shadow_bottom_right);
			this.shadowBottom = itemView.findViewById(R.id.v_shadow_bottom);
		}

		@Override
		public void onProgressUpdate()
		{
			EnhancedMediaPlayer player = getItem(this.getLayoutPosition());
			if (player != null)
				this.timePosition.setProgress(player.getCurrentPosition());
		}

		private void bindData(int positionInDataSet)
		{
			super.setToDeleteSettings(SoundboardPreferences.isOneSwipeToDeleteEnabled());

			EnhancedMediaPlayer player = getItem(positionInDataSet);
			if (player == null)
				return;

			MediaPlayerData data = player.getMediaPlayerData();
			super.showContentPage();

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


		@NonNull
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
			int position = this.getLayoutPosition();
			EnhancedMediaPlayer player = getItem(position);
			if (player != null && onItemDeleteListener != null)
				onItemDeleteListener.onItemDelete(player, position);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startProgressUpdateTimer();
				}
			}, 2 * UPDATE_INTERVAL);
		}

		@Override
		public void onTextEdited(String text)
		{
			EnhancedMediaPlayer player = getItem(this.getLayoutPosition());
			this.name.clearFocus();

			if (player != null && !text.equals(player.getMediaPlayerData().getLabel()))
			{
				player.getMediaPlayerData().setLabel(text);
				player.getMediaPlayerData().setItemWasAltered();

				eventBus.post(new OpenSoundRenameEvent(player.getMediaPlayerData()));
			}
		}

		@Override
		public void onClick(View view)
		{
			super.onClick(view);

			EnhancedMediaPlayer player = getItem(this.getLayoutPosition());
			if (player == null)
				return;
			boolean isSelected = view.isSelected();
			int id = view.getId();
			switch (id)
			{
				case R.id.b_stop:
					player.stopSound();
					notifyItemChanged(this.getLayoutPosition());
					break;
				case R.id.b_loop:
					view.setSelected(!isSelected);
					player.setLooping(!isSelected);
					break;
				case R.id.b_add_to_playlist:
					if (soundsDataStorage == null)
						return;

					view.setSelected(!isSelected);
					player.setIsInPlaylist(!isSelected);
					player.getMediaPlayerData().setItemWasAltered();
					soundsDataStorage.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), !isSelected);
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
						player.fadeOutSound();
					}
					break;
				case R.id.b_settings:
					player.pauseSound();
					eventBus.post(new OpenSoundSettingsEvent(player.getMediaPlayerData()));
					break;
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			if (fromUser)
			{
				EnhancedMediaPlayer player = getItem(this.getLayoutPosition());
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

	public interface OnItemDeleteListener
	{
		void onItemDelete(EnhancedMediaPlayer data, int position);
	}
}
