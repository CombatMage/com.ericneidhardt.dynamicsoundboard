package org.neidhardt.dynamicsoundboard.navigationdrawer;

import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeEvent;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;

/**
 * Created by eric.neidhardt on 26.05.2015.
 */
public abstract class NavigationDrawerListPresenter<T extends NavigationDrawerList>
		extends
			BaseViewPresenter<T>
		implements
			android.support.v7.view.ActionMode.Callback
{
	private static final String TAG = NavigationDrawerListPresenter.class.getName();

	protected boolean isInSelectionMode;

	private SparseArray<View> selectedItems;

	protected void onItemSelected(View view, int indexOfSelectedItem)
	{
		if (view.isSelected())
			this.selectedItems.remove(indexOfSelectedItem);
		else
			this.selectedItems.put(indexOfSelectedItem, view);

		this.getBus().post(new ActionModeEvent(this, ActionModeEvent.REQUEST.INVALIDATE));
		view.setSelected(!view.isSelected());
	}

	public void prepareItemDeletion()
	{
		this.getBus().post(new ActionModeEvent(this, ActionModeEvent.REQUEST.START));
	}

	public void deleteSelected()
	{
		this.getBus().post(new ActionModeEvent(this, ActionModeEvent.REQUEST.STOP));
		this.getView().onDeleteSelected(selectedItems);
	}

	@Override
	public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onCreateActionMode failed, supplied view is null ");

		this.isInSelectionMode = true;
		this.selectedItems = new SparseArray<>(this.getView().getItemCount());
		return true;
	}

	@Override
	public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null ");

		actionMode.setTitle(this.getView().getActionModeTitle());

		int count = this.selectedItems.size();
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
		for(int i = 0; i < this.selectedItems.size(); i++)
			this.selectedItems.valueAt(i).setSelected(false);

		this.getBus().post(new ActionModeEvent(this, ActionModeEvent.REQUEST.STOPPED));
	}

	public abstract void onDeleteSelected(SparseArray<View> selectedItems);

	interface OnNavigationDrawerListEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a request to change the current contextual action mode has benn submitted.
		 * @param event delivered OpenSoundSheetEvent
		 */
		void onEvent(ActionModeEvent event);
	}
}
