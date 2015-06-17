package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
public interface ListAdapter<T>
{
	void onAttachedToWindow();

	void onDetachedFromWindow();

	void notifyItemChanged(T data);
}
