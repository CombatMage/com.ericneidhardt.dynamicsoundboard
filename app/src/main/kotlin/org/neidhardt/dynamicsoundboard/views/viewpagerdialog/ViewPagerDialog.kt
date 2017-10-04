package org.neidhardt.dynamicsoundboard.views.viewpagerdialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.neidhardt.dynamicsoundboard.base.BaseDialogFragment
import org.neidhardt.dynamicsoundboard.views.viewpagerdialog.viewhelper.ViewPagerDialogBuilder

/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
abstract class ViewPagerDialog : BaseDialogFragment() {

	private val KEY_VIEW_DATA = "KEY_VIEW_DATA"

	private lateinit var viewData: Array<String>

	fun setViewData(viewData: Array<String>) {
		this.arguments = Bundle().apply {
			this.putStringArray(KEY_VIEW_DATA, viewData)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.viewData = this.arguments?.getStringArray(KEY_VIEW_DATA) ?: emptyArray()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = ViewPagerDialogBuilder(this.activity)

		this.getStringTitle()?.let { dialogBuilder.setTitle(it) }
		this.getStringMessage()?.let { dialogBuilder.setMessage(it) }

		//dialogBuilder.setDataToDisplay(this.viewData)

		this.getStringButtonOk()?.let {
			dialogBuilder.setPositiveButton(it, { _, _ ->
				this.onButtonClicked()
			})
		}

		return dialogBuilder.create()
	}

	open fun getStringTitle(): String? = null

	open fun getStringMessage(): String? = null

	open fun getStringButtonOk(): String? = null

	open fun onButtonClicked() {
		val activity = this.activity
		if (activity is ViewPagerDialogActivity) {
			activity.onViewPagerDialogButtonClicked()
		}
	}

	interface ViewPagerDialogActivity {
		fun onViewPagerDialogButtonClicked()
	}
}