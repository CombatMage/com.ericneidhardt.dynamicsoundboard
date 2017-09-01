package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper

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

abstract class NavigationDrawerListBasePresenter : NavigationDrawerListPresenter {

	var isInSelectionMode: Boolean = false

	override fun startDeletionMode() {
		this.isInSelectionMode = true
	}

	override fun stopDeletionMode() {
		this.isInSelectionMode = false
		this.deselectAllItemsSelectedForDeletion()
	}

	override abstract val itemCount: Int

	protected abstract val numberOfItemsSelectedForDeletion: Int

	protected abstract fun deselectAllItemsSelectedForDeletion()
}
