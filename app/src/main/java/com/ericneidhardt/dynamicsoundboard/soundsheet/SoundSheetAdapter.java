package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetAdapter extends RecyclerView.Adapter<SoundSheetAdapter.ViewHolder>
{
	private List<SoundSheet> soundSheets;
	private OnItemClickListener onItemClickListener;
	private OnItemDeleteListener onItemDeleteListener;

	public SoundSheetAdapter()
	{
		this.soundSheets = new ArrayList<SoundSheet>();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	public void addAll(List<SoundSheet> soundSheets, boolean notifyDataSetChanged)
	{
		this.soundSheets.addAll(soundSheets);
		if (notifyDataSetChanged)
			this.notifyDataSetChanged();
	}

	public void remove(SoundSheet soundSheet, boolean notifyDataSetChanged)
	{
		int position = this.soundSheets.indexOf(soundSheet);
		this.soundSheets.remove(position);
		if (notifyDataSetChanged)
			this.notifyItemRemoved(position);
	}

	public void clear(boolean notifyDataSetChanged)
	{
		this.soundSheets.clear();
		if (notifyDataSetChanged)
			this.notifyDataSetChanged();
	}


	/**
	 * Set the item with this position selected and all other items deselected
	 * @param position index of item to be selected
	 */
	public void setSelectedItem(int position)
	{
		for (int i = 0; i < this.soundSheets.size(); i++)
		{
			boolean isSelected = i == position;
			this.soundSheets.get(i).setIsSelected(isSelected);
		}
		this.notifyDataSetChanged();
	}

	public List<SoundSheet> getValues()
	{
		return this.soundSheets;
	}

	@Override
	public int getItemCount()
	{
		return this.soundSheets.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sound_sheet_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		SoundSheet data = this.soundSheets.get(position);
		holder.textView.setText(data.getLabel());
		holder.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private TextView textView;
		private ImageView selectionIndicator;
		private ImageButton deleteItem;

		public ViewHolder(View itemView) {
			super(itemView);

			this.textView = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.deleteItem = (ImageButton) itemView.findViewById(R.id.ib_delete_sound_sheet);

			itemView.setOnClickListener(this);
			this.deleteItem.setOnClickListener(this);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (view.getId() == R.id.ib_delete_sound_sheet)
			{
				if (onItemDeleteListener != null)
					onItemDeleteListener.onItemDelete(view, soundSheets.get(position), position);
			}
			else
			{
				if (onItemClickListener != null)
					onItemClickListener.onItemClick(view, soundSheets.get(position), position);
			}
		}

	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, SoundSheet data, int position);
	}

	public static interface OnItemDeleteListener
	{
		public void onItemDelete(View view, SoundSheet data, int position);
	}

}
