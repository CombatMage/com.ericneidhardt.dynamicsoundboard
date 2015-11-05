package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.view.Menu
import android.view.MenuItem
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
abstract class NavigationDrawerListPresenter<T: NavigationDrawerList> : BaseViewPresenter<T>(), android.support.v7.view.ActionMode.Callback
{
	private val TAG = javaClass.name

	var isInSelectionMode: Boolean = false

	protected fun onItemSelectedForDeletion() = this.eventBus.post(ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.INVALIDATE))

	protected fun onSelectedItemsDeleted() = this.eventBus.post(ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.STOP))

	fun prepareItemDeletion() = this.eventBus.post(ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.START))

	override fun onCreateActionMode(actionMode: android.support.v7.view.ActionMode, menu: Menu): Boolean
	{
		if (this.view == null)
			throw NullPointerException(TAG + ".onCreateActionMode failed, supplied view is null ")

		this.isInSelectionMode = true
		return true
	}

	override fun onPrepareActionMode(actionMode: android.support.v7.view.ActionMode, menu: Menu): Boolean
	{
		if (this.view == null)
			throw NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null ")

		actionMode.setTitle(this.view.actionModeTitle)

		val count = this.numberOfItemsSelectedForDeletion
		var countString = Integer.toString(count)
		if (countString.length() == 1)
			countString = " " + countString
		countString = countString + "/" + this.view.itemCount

		actionMode.subtitle = countString
		return true
	}

	override fun onActionItemClicked(actionMode: android.support.v7.view.ActionMode, menuItem: MenuItem): Boolean = false

	override fun onDestroyActionMode(actionMode: android.support.v7.view.ActionMode)
	{
		this.isInSelectionMode = false
		this.deselectAllItemsSelectedForDeletion()

		this.eventBus.post(ActionModeChangeRequestedEvent(this, ActionModeChangeRequestedEvent.REQUEST.STOPPED))
	}

	abstract fun deleteSelectedItems()

	protected abstract val numberOfItemsSelectedForDeletion: Int

	protected abstract fun deselectAllItemsSelectedForDeletion()
}
