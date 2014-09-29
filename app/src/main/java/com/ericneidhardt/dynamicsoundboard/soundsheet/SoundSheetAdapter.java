package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetAdapter extends RecyclerView.Adapter<SoundSheetAdapter.ViewHolder>
{
	private List<SoundSheet> soundSheets;
	private OnItemClickListener onItemClickListener;
	private Fragment parent;

	public SoundSheetAdapter()
	{
		this.soundSheets = new ArrayList<SoundSheet>();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setParent(Fragment parent) {
		this.parent = parent;
	}

	public void addAll(List<SoundSheet> soundSheets)
	{
		this.soundSheets.addAll(soundSheets);
	}

	public void removeAll(List<SoundSheet> soundSheets)
	{
		this.soundSheets.removeAll(soundSheets);
	}

	public void clear()
	{
		this.soundSheets.clear();
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
		holder.label.setText(data.getLabel());
		holder.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);

		if (this.parent != null)
		{
			SoundManagerFragment fragment = (SoundManagerFragment) this.parent.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
			List<EnhancedMediaPlayer> sounds = fragment.getSounds().get(data.getFragmentTag());
			holder.setSoundCount(sounds != null ? sounds.size() : 0);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private TextView label;
		private ImageView selectionIndicator;
		private TextView soundCount;
		private View soundCountLabel;

		public ViewHolder(View itemView) {
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
			this.selectionIndicator = (ImageView)itemView.findViewById(R.id.iv_selected);
			this.soundCount = (TextView)itemView.findViewById(R.id.tv_sound_count);
			this.soundCountLabel = itemView.findViewById(R.id.tv_sound_count_label);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, soundSheets.get(position), position);
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

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, SoundSheet data, int position);
	}

}
