package org.neidhardt.dynamicsoundboard.soundlayouts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayoutListAdapter extends RecyclerView.Adapter<SoundLayoutListAdapter.ViewHolder>
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
		List<String> soundLayouts = this.getValues();
		int size = soundLayouts.size();
		for (int i = 0; i < size; i++)
		{
			boolean isSelected = i == position;
			// soundSheets.get(i).setIsSelected(isSelected); // TODO use real data object and set selection
		}
		this.notifyDataSetChanged();
	}

	public List<String> getValues()
	{
		/*if (this.parent == null || this.parent.getSoundSheetManagerFragment() == null)
			return new ArrayList<>();
		return this.parent.getSoundSheetManagerFragment().getAll();*/

		return new ArrayList<String>();
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
		// TODO use real data object
		// SoundSheet data = this.getValues().get(position);
		// holder.label.setText(data.getLabel());
		// holder.selectionIndicator.setVisibility(data.getIsSelected() ? View.VISIBLE : View.INVISIBLE);
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

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view)
		{
			int position = this.getPosition();
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(view, getValues().get(position), position);
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, String data, int position); // TODO wrong data
	}
}
