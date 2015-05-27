package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

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

import java.util.List;

/**
 * Created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayoutsListAdapter
		extends
			RecyclerView.Adapter<SoundLayoutsListAdapter.ViewHolder>
		implements
			SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
			AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{
	private OnItemClickListener onItemClickListener;
	private EventBus bus;

	public SoundLayoutsListAdapter()
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

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param position index of item to be selected
	 */
	public void setSelectedItem(int position)
	{
		SoundLayoutsManager.getInstance().setSelected(position);
		this.notifyDataSetChanged();
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
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sound_layout_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		SoundLayout data = this.getValues().get(position);
		holder.label.setText(data.getLabel());
		holder.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRenamedEvent event)
	{
		SoundLayout renamedLayout = event.getRenamedSoundLayout();
		this.notifyItemChanged(this.getValues().indexOf(renamedLayout));
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutAddedEvent event)
	{
		this.notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private TextView label;
		private ImageView selectionIndicator;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);

			itemView.findViewById(R.id.b_settings).setOnClickListener(this);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view)
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
