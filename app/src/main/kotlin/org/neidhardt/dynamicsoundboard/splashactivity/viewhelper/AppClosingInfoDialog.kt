package org.neidhardt.dynamicsoundboard.splashactivity.viewhelper

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 25.09.2017.
 */
class AppClosingInfoDialog : DialogFragment() {

	companion object {
		private val TAG = AppClosingInfoDialog::class.java.name

		fun show(fragmentManager: FragmentManager) {
			val dialog = AppClosingInfoDialog()
			dialog.show(fragmentManager, TAG)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.isCancelable = false
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val builder = AlertDialog.Builder(this.context)

		builder.setMessage(R.string.dialogappclosing_message)
		builder.setPositiveButton(R.string.dialog_ok) { _, _ ->
			this.dismiss()
			this.activity?.finish()
		}

		return builder.create()
	}
}