package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletion
import org.neidhardt.eventbus_utils.registerIfRequired

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
interface NavigationDrawerListPresenter {

	fun startDeletionMode()

	fun stopDeletionMode()

	fun selectAllItems()

	fun deleteSelectedItems()

	fun onAttachedToWindow()

	fun onDetachedFromWindow()

	val itemCount: Int
}

abstract class NavigationDrawerListBasePresenter<T: RecyclerView?> :
		NavigationDrawerListPresenter
{
	var isInSelectionMode: Boolean = false

	abstract var view: T

	abstract val eventBus: EventBus

	override fun onAttachedToWindow()
	{
		this.eventBus.registerIfRequired(this)
	}

	override fun onDetachedFromWindow()
	{
		this.eventBus.unregister(this)
	}

	override fun startDeletionMode()
	{
		this.isInSelectionMode = true
	}

	override fun stopDeletionMode()
	{
		this.isInSelectionMode = false
		this.deselectAllItemsSelectedForDeletion()
	}

	protected fun onItemSelectedForDeletion()
	{
		this.eventBus.post(ItemSelectedForDeletion(this.numberOfItemsSelectedForDeletion, this.itemCount))
	}

	override abstract val itemCount: Int

	protected abstract val numberOfItemsSelectedForDeletion: Int

	protected abstract fun deselectAllItemsSelectedForDeletion()
}
