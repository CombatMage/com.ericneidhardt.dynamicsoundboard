package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
interface NavigationDrawerListPresenter {

	fun startDeletionMode()

	fun stopDeletionMode()

	fun deleteSelectedItems()

	fun onAttachedToWindow()

	fun onDetachedFromWindow()
}

abstract class NavigationDrawerListBasePresenter<T: RecyclerView?> :
		NavigationDrawerListPresenter
{
	private val TAG = javaClass.name

	var isInSelectionMode: Boolean = false

	abstract var view: T

	abstract val eventBus: EventBus

	override fun onAttachedToWindow()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
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

	protected fun onItemSelectedForDeletion() {
		// TODO
	}

	protected abstract val numberOfItemsSelectedForDeletion: Int

	protected abstract fun deselectAllItemsSelectedForDeletion()
}
