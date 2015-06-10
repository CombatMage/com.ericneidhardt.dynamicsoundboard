package org.neidhardt.dynamicsoundboard.navigationdrawer.views;

import android.view.Menu;
import android.view.MenuItem;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public abstract class NavigationDrawerListPresenter<T extends NavigationDrawerList>
		extends
			BaseViewPresenter<T>
		implements
			android.support.v7.view.ActionMode.Callback
{
	private static final String TAG = NavigationDrawerListPresenter.class.getName();

	private boolean isInSelectionMode;

	protected void onItemSelectedForDeletion()
	{
		this.getEventBus().post(new ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.INVALIDATE));
	}

	public void prepareItemDeletion()
	{
		this.getEventBus().post(new ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.START));
	}

	protected void onSelectedItemsDeleted()
	{
		this.getEventBus().post(new ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.STOP));
	}

	@Override
	public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onCreateActionMode failed, supplied view is null ");

		this.isInSelectionMode = true;
		return true;
	}

	@Override
	public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null ");

		actionMode.setTitle(this.getView().getActionModeTitle());

		int count = this.getNumberOfItemsSelectedForDeletion();
		String countString = Integer.toString(count);
		if (countString.length() == 1)
			countString = " " + countString;
		countString = countString + "/" + this.getView().getItemCount();

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
		this.isInSelectionMode = false;
		this.deselectAllItemsSelectedForDeletion();

		this.getEventBus().post(new ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.STOPPED));
	}

	public abstract void deleteSelectedItems();

	protected abstract int getNumberOfItemsSelectedForDeletion();

	protected abstract void deselectAllItemsSelectedForDeletion();

	public boolean isInSelectionMode()
	{
		return isInSelectionMode;
	}

	public void setIsInSelectionMode(boolean isInSelectionMode)
	{
		this.isInSelectionMode = isInSelectionMode;
	}
}
