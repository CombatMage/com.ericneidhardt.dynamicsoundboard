package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListAdapter;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.SoundRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnSoundsChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsChangedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetRenamedEventListener;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetsChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetRenamedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsChangedEvent;

import java.util.List;

public class SoundSheetsAdapter
		extends
			RecyclerView.Adapter<SoundSheetsAdapter.ViewHolder>
		implements
			NavigationDrawerListAdapter<SoundSheet>,
			SoundSheetFragment.OnSoundRemovedEventListener,
			OnSoundSheetsChangedEventListener,
			OnSoundSheetRenamedEventListener,
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
	public void notifyItemChanged(SoundSheet data)
	{
		int index = this.getValues().indexOf(data);
		if (index == -1)
			this.notifyDataSetChanged();
		else
			this.notifyItemChanged(index);
	}

	@Override
	public void onEvent(SoundsChangedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEvent(SoundRemovedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEvent(SoundSheetRenamedEvent event)
	{
		this.notifyDataSetChanged();
	}

	@Override
	public void onEvent(SoundSheetsChangedEvent event)
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

			this.label.setActivated(data.isSelectedForDeletion());
			this.itemView.setSelected(data.isSelectedForDeletion());

		}

		@Override
		public void onClick(View view)
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
