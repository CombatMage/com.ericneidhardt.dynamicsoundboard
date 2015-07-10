package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import org.jetbrains.annotations.NotNull;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetsChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetAddedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetChangedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.BaseAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SoundSheetsAdapter
		extends
			BaseAdapter<SoundSheet, SoundSheetsAdapter.ViewHolder>
		implements
			OnSoundSheetsChangedEventListener,
			OnSoundsChangedEventListener
{
	private SoundSheetsPresenter presenter;
	private EventBus eventBus;
	private OnItemClickListener onItemClickListener;

	public SoundSheetsAdapter()
	{
		this.eventBus = EventBus.getDefault();
	}

	@Override
	public void onAttachedToWindow()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this);
		this.notifyDataSetChanged();
	}

	@Override
	public void onDetachedFromWindow()
	{
		this.eventBus.unregister(this);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	@NonNull
	public List<SoundSheet> getValues()
	{
		return this.presenter.getSoundSheets();
	}

	@Override
	public int getItemViewType(int position)
	{
		return R.layout.view_sound_sheet_item;
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
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
		SoundSheet data = this.getValues().get(position);

		List<EnhancedMediaPlayer> sounds = this.presenter.getSoundsInFragment(data.getFragmentTag());
		int soundCount = sounds != null ? sounds.size() : 0;

		holder.bindData(data, soundCount);
	}

	@Override
	public void onEventMainThread(@NonNull SoundAddedEvent event)
	{
		String fragmentTag = event.getPlayer().getMediaPlayerData().getFragmentTag();
		SoundSheet changedSoundSheet = this.presenter.getSoundSheetsDataAccess().getSoundSheetForFragmentTag(fragmentTag);

		this.notifyItemChanged(changedSoundSheet);
	}

	@Override
	public void onEventMainThread(@NonNull SoundsRemovedEvent event)
	{
		List<EnhancedMediaPlayer> removedPlayers = event.getPlayers();
		if (removedPlayers == null)
			this.notifyDataSetChanged();
		else
		{
			Set<String> affectedFragmentTags = new HashSet<>();
			for (EnhancedMediaPlayer player : removedPlayers)
				affectedFragmentTags.add(player.getMediaPlayerData().getFragmentTag());

			for (String fragmentTag : affectedFragmentTags) {
				SoundSheet changedSoundSheet = this.presenter.getSoundSheetsDataAccess().getSoundSheetForFragmentTag(fragmentTag);
				this.notifyItemChanged(changedSoundSheet);
			}
		}
	}

	@Override
	public void onEventMainThread(@NonNull SoundChangedEvent event) {}

	@Override
	public void onEventMainThread(@NonNull SoundMovedEvent event) {}

	@Override
	public void onEventMainThread(@NotNull SoundSheetAddedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEventMainThread(@NotNull SoundSheetChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEventMainThread(@NotNull SoundSheetsRemovedEvent event)
	{
		this.notifyDataSetChanged();
	}

	void setPresenter(SoundSheetsPresenter presenter)
	{
		this.presenter = presenter;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private View itemView;
		private TextView label;
		private ImageView selectionIndicator;
		private TextView soundCount;
		private View soundCountLabel;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.itemView = itemView;
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.soundCount = (TextView)itemView.findViewById(R.id.tv_sound_count);
			this.soundCountLabel = itemView.findViewById(R.id.tv_sound_count_label);

			itemView.setOnClickListener(this);
		}

		public void bindData(SoundSheet data, int soundCount)
		{
			this.label.setText(data.getLabel());
			this.setSoundCount(soundCount);

			this.label.setSelected(data.getIsSelected());
			this.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);

			this.label.setActivated(data.getIsSelectedForDeletion());
			this.itemView.setSelected(data.getIsSelectedForDeletion());

		}

		@Override
		public void onClick(@NonNull View view)
		{
			int position = this.getLayoutPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, getValues().get(position), position);
		}

		private void setSoundCount(int soundCount)
		{
			if (soundCount == 0)
			{
				this.soundCount.setVisibility(View.INVISIBLE);
				this.soundCountLabel.setVisibility(View.INVISIBLE);
			}
			else
			{
				this.soundCountLabel.setVisibility(View.VISIBLE);
				this.soundCount.setVisibility(View.VISIBLE);
				this.soundCount.setText(Integer.toString(soundCount));
			}
		}
	}

	public interface OnItemClickListener
	{
		void onItemClick(View view, SoundSheet data, int position);
	}

}
