package org.neidhardt.dynamicsoundboard.soundsheet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetsAdapter extends RecyclerView.Adapter<SoundSheetsAdapter.ViewHolder>
{
	private NavigationDrawerFragment parent;
	private OnItemClickListener onItemClickListener;

	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setNavigationDrawerFragment(NavigationDrawerFragment parent)
	{
		this.parent = parent;
	}

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param position index of item to be selected
	 */
	public void setSelectedItem(int position)
	{
		List<SoundSheet> soundSheets = this.getValues();
		int size = soundSheets.size();
		for (int i = 0; i < size; i++)
		{
			boolean isSelected = i == position;
			soundSheets.get(i).setIsSelected(isSelected);
		}
		this.notifyDataSetChanged();
	}

	public List<SoundSheet> getValues()
	{
		if (this.parent == null || this.parent.getSoundSheetManagerFragment() == null)
			return new ArrayList<>();
		return this.parent.getSoundSheetManagerFragment().getAll();
	}

	@Override
	public int getItemCount()
	{
		return this.getValues().size();
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
		SoundSheet data = this.getValues().get(position);
		holder.label.setText(data.getLabel());
		holder.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);

		if (this.parent != null)
		{
			ServiceManagerFragment fragment = this.parent.getServiceManagerFragment();
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

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, SoundSheet data, int position);
	}

}
