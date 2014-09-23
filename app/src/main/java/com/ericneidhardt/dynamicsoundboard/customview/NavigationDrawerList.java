package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;


public abstract class NavigationDrawerList extends FrameLayout implements ActionMode.Callback
{
	protected NavigationDrawerFragment parent;

	protected ActionMode actionMode;

	public NavigationDrawerList(Context context) {
		super(context);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavigationDrawerList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void prepareItemDeletion()
	{
		if (this.parent == null)
			throw new NullPointerException("Cannot prepare deletion, because the containing fragment is null");

		this.actionMode = this.parent.getActivity().startActionMode(this);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		ContextualActionbar actionbar = new ContextualActionbar(this.getContext());
		actionbar.setDeleteAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDeleteSelected();
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
		return true;
	}

	protected void onSelectAll()
	{
		// TODO
	}

	protected void onDeleteSelected()
	{
		// TODO
	}

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
	}

	protected abstract int getItemCount();
}
