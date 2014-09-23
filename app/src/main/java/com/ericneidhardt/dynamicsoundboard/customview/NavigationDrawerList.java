package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;

import java.util.HashMap;
import java.util.Map;


public abstract class NavigationDrawerList
		extends
			FrameLayout
		implements
			ActionMode.Callback
{
	protected NavigationDrawerFragment parent;
	protected ActionMode actionMode;
	protected boolean isInSelectionMode;

	private ContextualActionbar actionbar;
	private Map<Integer, View> selectedItems;

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

		this.actionbar.setNumberOfSelectedItems(this.selectedItems.size(), this.getItemCount());
		view.setSelected(!view.isSelected());
	}

	public void prepareItemDeletion()
	{
		if (this.parent == null)
			throw new NullPointerException("Cannot prepare deletion, because the containing fragment is null");

		this.actionMode = this.parent.getActivity().startActionMode(this);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		this.actionbar = new ContextualActionbar(this.getContext());
		actionbar.setDeleteAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionMode.finish();
				onDeleteSelected(selectedItems);
			}
		});
		actionbar.setSelectAllAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSelectAll();
			}
		});
		actionbar.setNumberOfSelectedItems(0, this.getItemCount());

		mode.setCustomView(actionbar);
		this.parent.onActionModeStart();
		this.isInSelectionMode = true;
		this.selectedItems = new HashMap<Integer, View>(this.getItemCount());

		return true;
	}

	protected void onSelectAll()
	{
		// TODO
	}

	protected abstract void onDeleteSelected(Map<Integer, View> selectedItems);

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
	{
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode)
	{
		this.actionMode = null;
		this.parent.onActionModeFinished();
		this.isInSelectionMode = false;
		for (int index : this.selectedItems.keySet())
			this.selectedItems.get(index).setSelected(false);
	}

	protected abstract int getItemCount();
}
