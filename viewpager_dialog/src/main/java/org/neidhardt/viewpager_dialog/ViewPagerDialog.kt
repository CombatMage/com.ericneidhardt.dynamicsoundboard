package org.neidhardt.viewpager_dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import org.neidhardt.viewpager_dialog.viewhelper.ViewPagerDialogBuilder

/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
open class ViewPagerDialog : DialogFragment() {

	companion object {

		private val TAG = ViewPagerDialog::class.java.name
		private val KEY_VIEW_DATA = "KEY_VIEW_DATA"

		fun showDialog(fragmentManager: FragmentManager, viewData: Array<String>) {
			val dialog = ViewPagerDialog()
			dialog.arguments = Bundle().apply {
				this.putStringArray(KEY_VIEW_DATA, viewData)
			}
			dialog.show(fragmentManager, TAG)
		}
	}

	private lateinit var viewData: Array<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.viewData = this.arguments?.getStringArray(KEY_VIEW_DATA) ?: emptyArray()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = ViewPagerDialogBuilder(this.activity)

		this.getStringTitle()?.let { dialogBuilder.setTitle(it) }
		this.getStringMessage()?.let { dialogBuilder.setMessage(it) }

		dialogBuilder.setDataToDisplay(this.viewData)

		this.getStringButtonOk()?.let {
			dialogBuilder.setPositiveButton(it, { _, _ ->
				this.onButtonClicked()
			})
		}

		return dialogBuilder.create()
	}

	protected fun getStringTitle(): String? = null

	protected fun getStringMessage(): String? = null

	protected fun getStringButtonOk(): String? = null

	protected fun onButtonClicked() {
		val activity = this.activity
		if (activity is ViewPagerDialogActivity) {
			activity.onViewPagerDialogButtonClicked()
		}
	}

	interface ViewPagerDialogActivity {
		fun onViewPagerDialogButtonClicked()
	}
}