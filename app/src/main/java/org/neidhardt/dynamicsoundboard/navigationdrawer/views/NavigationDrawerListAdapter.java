package org.neidhardt.dynamicsoundboard.navigationdrawer.views;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
public interface NavigationDrawerListAdapter<T>
{
	void onAttachedToWindow();

	void onDetachedFromWindow();

	void notifyItemChanged(T data);
}
