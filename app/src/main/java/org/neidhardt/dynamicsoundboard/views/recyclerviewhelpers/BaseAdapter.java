package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers;

import android.support.v7.widget.RecyclerView;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * File created by eric.neidhardt on 17.06.2015.
 */
public abstract class BaseAdapter<Type, ViewHolder extends RecyclerView.ViewHolder>
		extends
			RecyclerView.Adapter<ViewHolder>
		implements
			ListAdapter<Type>
{
	public abstract List<Type> getValues();

	@Override
	public void notifyItemChanged(Type data)
	{
		int index = this.getValues().indexOf(data);
		if (index == -1)
			this.notifyDataSetChanged();
		else
			this.notifyItemChanged(index);
	}
}
