package org.neidhardt.dynamicsoundboard.soundactivity.viewhelper

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.misc.IntentRequest

/**
 * File created by eric.neidhardt on 22.10.2015.
 */
private val KEY_MESSAGE_ID = "KEY_MESSAGE_ID"
private val KEY_PERMISSION = "KEY_PERMISSION"
private val KEY_REQUEST_PERMISSION_ID = "KEY_REQUEST_PERMISSION_ID"
private val KEY_CLOSE_ON_DENIAL = "KEY_CLOSE_ON_DENIAL"

fun AppCompatActivity.explainReadPhoneStatePermission() {
	OldExplainPermissionDialog.show(this.supportFragmentManager,
			R.string.request_permission_read_phone_state_message,
			Manifest.permission.READ_PHONE_STATE,
			IntentRequest.REQUEST_PERMISSION_READ_PHONE_STATE)
}

fun AppCompatActivity.explainReadStoragePermission() {
	OldExplainPermissionDialog.show(this.supportFragmentManager,
			R.string.request_permission_read_storage_message,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			IntentRequest.REQUEST_PERMISSION_READ_STORAGE,
			true)
}

fun AppCompatActivity.explainWriteStoragePermission() {
	OldExplainPermissionDialog.show(this.supportFragmentManager,
			R.string.request_permission_write_storage_message,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			IntentRequest.REQUEST_PERMISSION_WRITE_STORAGE,
			true)
}

class OldExplainPermissionDialog : BaseDialog() {

	private var messageId = 0
	private var permission = ""
	private var requestId = 0
	private var closeAppOnDenial = true

	companion object {

		private val TAG = BaseDialog::javaClass.name

		fun show(fragmentManager: FragmentManager, messageId: Int, permission: String, requestId: Int, closeAppOnDenial: Boolean = false) {
			val dialog = OldExplainPermissionDialog()
			val args = Bundle()
			args.putInt(KEY_MESSAGE_ID, messageId)
			args.putBoolean(KEY_CLOSE_ON_DENIAL, closeAppOnDenial)
			args.putString(KEY_PERMISSION, permission)
			args.putInt(KEY_REQUEST_PERMISSION_ID, requestId)

			dialog.arguments = args
			dialog.show(fragmentManager, TAG)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val args = this.arguments
		if (args != null) {
			this.messageId = args.getInt(KEY_MESSAGE_ID)
			this.closeAppOnDenial = args.getBoolean(KEY_CLOSE_ON_DENIAL)
			this.permission = args.getString(KEY_PERMISSION)
			this.requestId = args.getInt(KEY_REQUEST_PERMISSION_ID)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialogBuilder = AlertDialog.Builder(this.activity)
		dialogBuilder.setTitle(R.string.request_permission_title)
		dialogBuilder.setMessage(this.messageId)

		if (this.closeAppOnDenial)
			dialogBuilder.setPositiveButton(R.string.dialog_close, { _, _ -> this.activity?.finish() })
		else {
			dialogBuilder.setPositiveButton(R.string.dialog_grant, { _, _ -> this.requestPermission() })
			dialogBuilder.setNegativeButton(R.string.dialog_denial, null)
		}

		return dialogBuilder.create()
	}

	private fun requestPermission() {
		val activity = this.activity
		if (activity != null)
			ActivityCompat.requestPermissions(activity, arrayOf(this.permission), this.requestId)
		this.dismiss()
	}
}