package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;


public abstract class NavigationDrawerList
		extends
			FrameLayout
		implements
			android.support.v7.view.ActionMode.Callback
{
	protected NavigationDrawerFragment parent;
	protected android.support.v7.view.ActionMode actionMode;
	protected boolean isInSelectionMode;

	private SparseArray<View> selectedItems;

	public NavigationDrawerList(Context context) {
		super(context);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void onItemSelected(View view, int indexOfSelectedItem)
	{
		if (view.isSelected())
			this.selectedItems.remove(indexOfSelectedItem);
		else
			this.selectedItems.put(indexOfSelectedItem, view);

		this.actionMode.invalidate(); // update item count in cab

		view.setSelected(!view.isSelected());
	}

	public void prepareItemDeletion()
	{
		if (this.parent == null)
			throw new NullPointerException("Cannot prepare deletion, because the containing fragment is null");

		this.actionMode = this.parent.getBaseActivity().startSupportActionMode(this);
	}

	public void deleteSelected()
	{
		actionMode.finish();
		onDeleteSelected(selectedItems);
	}

	protected abstract void onDeleteSelected(SparseArray<View> selectedItems);

	protected abstract int getItemCount();

	protected abstract int getActionModeTitle();

	@Override
	public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		this.parent.onActionModeStart();
		this.isInSelectionMode = true;
		this.selectedItems = new SparseArray<View>(this.getItemCount());

		return true;
	}

	@Override
	public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		actionMode.setTitle(this.getActionModeTitle());

		int count = this.selectedItems.size();
		String countString = Integer.toString(count);
		if (countString.length() == 1)
			countString = " " + countString;
		countString = countString + "/" + this.getItemCount();

		actionMode.setSubtitle(countString);
		return true;
	}

	@Override
	public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem)
	{
		return false;
	}

	@Override
	public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode)
	{
		this.actionMode = null;
		this.parent.onActionModeFinished();
		this.isInSelectionMode = false;
		for(int i = 0; i < this.selectedItems.size(); i++)
			this.selectedItems.valueAt(i).setSelected(false);
	}
}
