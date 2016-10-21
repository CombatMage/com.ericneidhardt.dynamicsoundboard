package org.neidhardt.ui_utils.helper

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import org.neidhardt.ui_utils.presenter.ViewPresenter

/**
 * File created by eric.neidhardt on 15.04.2016.
 */
interface SnackbarView {

	fun showSnackbar(messageId: Int, duration: Int, action: SnackbarView.SnackbarAction? = null)

	fun showSnackbar(message: String, duration: Int, action: SnackbarView.SnackbarAction? = null)

	fun dismissSnackbar()

	class SnackbarAction(val labelId: Int, val action: ((View) -> Unit))
}

class SnackbarPresenter : ViewPresenter, SnackbarView {

	private val TAG = javaClass.name

	private var coordinatorLayout: CoordinatorLayout? = null
	private var snackbar: Snackbar? = null

	fun init(coordinatorLayout: CoordinatorLayout) {
		this.coordinatorLayout = coordinatorLayout
	}

	override fun stop() {
		super.stop()
		this.snackbar?.dismiss()
	}

	override fun showSnackbar(messageId: Int, duration: Int, action: SnackbarView.SnackbarAction?) {
		this.coordinatorLayout?.let {
			val message = it.context.resources.getString(messageId)
			this.showSnackbar(message, duration, action)
		} ?: Log.e(TAG, "Could not show snackbar, no coordinator given")
	}

	override fun showSnackbar(message: String, duration: Int, action: SnackbarView.SnackbarAction?) {
		this.snackbar?.dismiss()
		this.coordinatorLayout?.let { coordinatorLayout ->
			this.snackbar = Snackbar.make(coordinatorLayout, message, duration).apply {
				if (action != null) this.setAction(action.labelId, action.action)
			}.apply { this.show() }
		} ?: Log.e(TAG, "Could not show snackbar, no coordinator given")
	}

	override fun dismissSnackbar() {
		this.snackbar?.dismiss()
	}
}