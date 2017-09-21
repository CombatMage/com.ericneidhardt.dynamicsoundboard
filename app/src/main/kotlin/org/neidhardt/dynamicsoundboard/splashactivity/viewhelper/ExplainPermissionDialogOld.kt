package org.neidhardt.dynamicsoundboard.splashactivity.viewhelper

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.base.BaseDialog

/**
 * Created by eric.neidhardt@gmail.com on 07.09.2017.
 */
class ExplainPermissionDialogOld : BaseDialog() {

	companion object {
		private val TAG = ExplainPermissionDialogOld::javaClass.name

		fun show(fragmentManager: FragmentManager) {
			val dialog = ExplainPermissionDialogOld()
			dialog.show(fragmentManager, TAG)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = AlertDialog.Builder(this.activity)
		//dialogBuilder.setTitle(R.string.request_permission_title)
		dialogBuilder.setMessage(this.getMessageForPermissions())

		dialogBuilder.setPositiveButton(R.string.dialog_ok, { _,_ ->
			this.requestMissingPermissions()
		})

		return dialogBuilder.create()
	}

	private fun getMessageForPermissions(): String {
		val activity = this.splashActivity
		val missingPermissions = activity.getMissingPermissions()

		var message = "TODO"
		missingPermissions.forEach { perm ->
			message += perm + "\n"
		}

		return message
	}

	private fun requestMissingPermissions() {
		val activity = this.splashActivity
		val missingPermissions = activity.getMissingPermissions()
		activity.requestPermissions(missingPermissions)
	}
}