package org.neidhardt.dynamicsoundboard.views

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.view.View

/**
 * File created by eric.neidhardt on 15.04.2016.
 */
interface SnackbarPresenter
{
	val coordinatorLayout: CoordinatorLayout

	fun makeSnackbar(messageId: Int, duration: Int, action: SnackbarAction?): Snackbar
	{
		val message = this.coordinatorLayout.context.resources.getString(messageId)
		return this.makeSnackbar(message, duration, action)
	}

	fun makeSnackbar(message: String, duration: Int, action: SnackbarAction?): Snackbar
	{
		return Snackbar.make(this.coordinatorLayout, message, duration).apply {
			if (action != null) this.setAction(action.label, action.action)
		}
	}

	class SnackbarAction(val label: String, val action: ((View) -> Unit))
}