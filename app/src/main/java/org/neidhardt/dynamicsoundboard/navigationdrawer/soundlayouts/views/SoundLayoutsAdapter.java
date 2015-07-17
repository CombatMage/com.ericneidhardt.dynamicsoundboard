package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutAddedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.ListAdapter;

import java.util.List;

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayoutsAdapter
		extends
			RecyclerView.Adapter<SoundLayoutsAdapter.ViewHolder>
		implements
		ListAdapter<SoundLayout>,
			SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
			AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{
	private OnItemClickListener onItemClickListener;
	private EventBus bus;

	public SoundLayoutsAdapter()
	{
		this.bus = EventBus.getDefault();
	}

	public void onAttachedToWindow()
	{
		if (!this.bus.isRegistered(this))
			this.bus.register(this);
		this.notifyDataSetChanged();
	}

	public void onDetachedFromWindow()
	{
		this.bus.unregister(this);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public List<SoundLayout> getValues()
	{
		return SoundLayoutsManager.getInstance().getSoundLayouts();
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
	}

	@Override
	public int getItemViewType(int position)
	{
		return R.layout.view_sound_layout_item;
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
		SoundLayout data = this.getValues().get(position);
		holder.bindData(data);
	}

	@Override
	public void notifyItemChanged(SoundLayout data)
	{
		int index = this.getValues().indexOf(data);
		if (index == -1)
			this.notifyDataSetChanged();
		else
			this.notifyItemChanged(index);
	}

	@Override
	public void onEvent(SoundLayoutRenamedEvent event)
	{
		SoundLayout renamedLayout = event.getRenamedSoundLayout();
		this.notifyItemChanged(this.getValues().indexOf(renamedLayout));
	}

	@Override
	public void onEvent(SoundLayoutAddedEvent event)
	{
		this.notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private View itemView;
		private TextView label;
		private ImageView selectionIndicator;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.itemView = itemView;
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);

			itemView.findViewById(R.id.b_settings).setOnClickListener(this);
			itemView.setOnClickListener(this);
		}

		public void bindData(SoundLayout data)
		{
			this.label.setText(data.getLabel());
			this.label.setSelected(data.getIsSelected());
			this.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);

			this.label.setActivated(data.isSelectedForDeletion());
			this.itemView.setSelected(data.isSelectedForDeletion());
		}

		@Override
		public void onClick(@NonNull View view)
		{
			if (onItemClickListener == null)
				return;

			int id = view.getId();
			int position = this.getLayoutPosition();
			SoundLayout item = getValues().get(position);
			if (id == R.id.b_settings)
				onItemClickListener.onItemSettingsClicked(item);
			else if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, item, position);
		}
	}

	public interface OnItemClickListener
	{
		void onItemClick(View view, SoundLayout data, int position);

		void onItemSettingsClicked(SoundLayout data);
	}

}
