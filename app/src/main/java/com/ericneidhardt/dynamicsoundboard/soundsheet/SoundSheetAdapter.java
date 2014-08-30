package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

	public SoundSheetAdapter()
	{
		this.soundSheets = new ArrayList<SoundSheet>();
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

	public void removeFragmentWithId(SoundSheet soundSheet)
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
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_drawer_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		String label = this.soundSheets.get(position).getLabel();
		holder.textView.setText(label);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		private TextView textView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.textView = (TextView)itemView.findViewById(R.id.tv_label);
		}
	}

}
