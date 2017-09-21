package org.neihdardt.viewpagerdialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.neihdardt.viewpagerdialog.viewhelper.ViewPagerDialogBuilder

/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
abstract class ViewPagerDialog : DialogFragment() {

	private val KEY_TITLE = "KEY_TITLE"
	private val KEY_MESSAGE = "KEY_MESSAGE"
	private val KEY_VIEW_DATA = "KEY_VIEW_DATA"

	private var title: String? = null
	private var message: String? = null
	private lateinit var viewData: Array<String>

	fun setViewData(viewData: Array<String>) {
		val args = this.arguments ?: Bundle()
		args.putStringArray(KEY_VIEW_DATA, viewData)
		this.arguments = args
	}

	fun setTitle(title: String) {
		val args = this.arguments ?: Bundle()
		args.putString(KEY_TITLE, title)
		this.arguments = args
	}

	fun setMessage(message: String) {
		val args = this.arguments ?: Bundle()
		args.putString(KEY_MESSAGE, message)
		this.arguments = args
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.title = this.arguments?.getString(KEY_TITLE)
		this.message = this.arguments?.getString(KEY_MESSAGE)
		this.viewData = this.arguments?.getStringArray(KEY_VIEW_DATA) ?: emptyArray()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = ViewPagerDialogBuilder(this.activity)

		this.title?.let { dialogBuilder.setTitle(it) }
		this.message?.let { dialogBuilder.setMessage(it) }

		dialogBuilder.setDataToDisplay(this.viewData)

		dialogBuilder.setOnKeyListener { _, keyCode, _ ->
			if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK)) {
				this.onBackPressed()
				this.dismiss()
				true
			} else {
				false
			}
		}

		this.getStringButtonOk()?.let {
			dialogBuilder.setPositiveButton(it, { _, _ ->
				this.onButtonClicked()
			})
		}

		return dialogBuilder.create()
	}

	open fun getStringButtonOk(): String? = null

	private fun onButtonClicked() {
		val activity = this.activity
		if (activity is ViewPagerDialogActivity) {
			activity.onViewPagerDialogButtonClicked()
		}
	}

	private fun onBackPressed() {
		val activity = this.activity
		if (activity is ViewPagerDialogActivity) {
			activity.onViewPagerDialogBackPressed()
		}
	}

	interface ViewPagerDialogActivity {
		fun onViewPagerDialogButtonClicked()
		fun onViewPagerDialogBackPressed()
	}
}