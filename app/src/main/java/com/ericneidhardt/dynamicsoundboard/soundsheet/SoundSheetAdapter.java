package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DismissibleItemViewHolder;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.ArrayList;
import java.util.List;

public class SoundSheetAdapter extends RecyclerView.Adapter<SoundSheetAdapter.ViewHolder>
{
	private static final int VIEWPAGER_INDEX_SOUND_SHEET = 0;
	private static final int UPDATE_INTERVAL = 500;

	private static final Handler handler = new Handler();

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

	public void addAll(List<SoundSheet> soundSheets)
	{
		this.soundSheets.addAll(soundSheets);
	}

	public void remove(SoundSheet soundSheet)
	{
		int position = this.soundSheets.indexOf(soundSheet);
		this.soundSheets.remove(position);
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
	}

	public class ViewHolder extends DismissibleItemViewHolder implements View.OnClickListener
	{
		private TextView label;
		private ImageView selectionIndicator;
		private ImageButton deleteItem;

		public ViewHolder(View itemView) {
			super(itemView);
			this.label = (TextView)itemView.findViewById(R.id.tv_label);
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
					onItemDeleteListener.onItemDelete(soundSheets.get(position), position);
			}
			else
			{
				if (onItemClickListener != null)
					onItemClickListener.onItemClick(view, soundSheets.get(position), position);
			}
		}

		@Override
		protected int getIndexOfContentPage()
		{
			return VIEWPAGER_INDEX_SOUND_SHEET;
		}

		@Override
		protected PagerAdapter getPagerAdapter()
		{
			return new SoundSheetPagerAdapter();
		}

		@Override
		public void onPageSelected(final int selectedPage)
		{
			final int position = this.getPosition();
			handler.postDelayed(new Runnable() { // delay deletion, because page is selected before scrolling has settled
				@Override
				public void run() {
					if (selectedPage != VIEWPAGER_INDEX_SOUND_SHEET && onItemDeleteListener != null)
						onItemDeleteListener.onItemDelete(soundSheets.get(position), position);
				}
			}, UPDATE_INTERVAL);
		}

		@Override
		public void onPageScrolled(int i, float v, int i1) {}

		@Override
		public void onPageScrollStateChanged(int i) {}
	}

	private class SoundSheetPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			if (position == VIEWPAGER_INDEX_SOUND_SHEET)
				return container.findViewById(R.id.layout_sound_sheet);
			else
				return container.findViewById(R.id.layout_remove_left);
		}
	}

	public static interface OnItemClickListener
	{
		public void onItemClick(View view, SoundSheet data, int position);
	}

	public static interface OnItemDeleteListener
	{
		public void onItemDelete(SoundSheet data, int position);
	}

}
