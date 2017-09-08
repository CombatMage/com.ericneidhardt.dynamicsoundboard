package org.neidhardt.viewpager_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager

/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
class ViewPagerDialog : DialogFragment(), ViewPagerDialogContract.View<String> {

	private val TAG = javaClass.name

	companion object {
		fun showDialog(fragmentManager: FragmentManager) {
			// TODO
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = AlertDialog.Builder(this.activity)
		dialogBuilder.setTitle(R.string.request_permission_title)
		dialogBuilder.setMessage(this.getMessageForPermissions())

		dialogBuilder.setPositiveButton(R.string.dialog_ok, { _,_ ->
			this.requestMissingPermissions()
		})

		return dialogBuilder.create()
	}

	override fun closeDialog() {
		this.dismiss()
	}

	override fun setDisplayedContent(viewData: Array<String>) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}