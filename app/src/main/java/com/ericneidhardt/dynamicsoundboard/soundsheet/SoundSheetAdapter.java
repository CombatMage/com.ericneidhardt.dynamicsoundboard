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

/**
 * Created by eric.neidhardt on 29.08.2014.
 */
public class SoundSheetAdapter extends RecyclerView.Adapter<SoundSheetAdapter.ViewHolder>
{
	private List<SoundSheet> soundSheets;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private OnItemDeleteListener onItemDeleteListener;

	public SoundSheetAdapter()
	{
		this.soundSheets = new ArrayList<SoundSheet>();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)
	{
		this.onItemLongClickListener = onItemLongClickListener;
	}

	public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener)
	{
		this.onItemDeleteListener = onItemDeleteListener;
	}

	public void add(SoundSheet soundSheet)
	{
		this.soundSheets.add(soundSheet);
		this.notifyItemInserted(this.soundSheets.size());
	}

	public void addAll(List<SoundSheet> soundSheets)
	{
		this.soundSheets.addAll(soundSheets);
		this.notifyDataSetChanged();
	}

	public void remove(SoundSheet soundSheet)
	{
		int position = this.soundSheets.indexOf(soundSheet);
		this.soundSheets.remove(position);
		this.notifyItemRemoved(position);
	}

	public void clear()
	{
		this.soundSheets.clear();
		this.notifyDataSetChanged();
	}

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param position
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

	public SoundSheet getSelectedItem()
	{

		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getIsSelected())
				return soundSheet;
		}
		return null;
	}

	public List<SoundSheet> getValues()
	{
		return this.soundSheets;
	}

	public SoundSheet get(String soundSheetTag)
	{
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getFragmentTag().equals(soundSheetTag))
				return soundSheet;
		}
		return null;
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

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
	{
		private TextView textView;
		private ImageView selectionIndicator;
		private ImageButton deleteItem;

		public ViewHolder(View itemView) {
			super(itemView);

			this.textView = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.deleteItem = (ImageButton)itemView.findViewById(R.id.ib_delete_sound_sheet);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
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

		@Override
		public boolean onLongClick(View view)
		{
			int position = this.getPosition();
			if (onItemLongClickListener != null)
				return onItemLongClickListener.onItemLongClick(view, soundSheets.get(position), position);
			return false;
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, SoundSheet data, int position);
	}

	public static interface OnItemLongClickListener
	{
		public boolean onItemLongClick(View view, SoundSheet data, int position);
	}

	public static interface OnItemDeleteListener
	{
		public void onItemDelete(View view, SoundSheet data, int position);
	}

}
