package org.neidhardt.dynamicsoundboard.soundactivity

import android.app.AlertDialog
import android.app.Dialog
import android.app.FragmentManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseDialog

/**
 * File created by eric.neidhardt on 22.10.2015.
 */
public class ExplainPermissionDialog : BaseDialog
{
	private val TAG = javaClass.name

	private val KEY_MESSAGE_ID = "KEY_MESSAGE_ID"
	private val KEY_PERMISSION = "KEY_PERMISSION"
	private val KEY_REQUEST_PERMISSION_ID = "KEY_REQUEST_PERMISSION_ID"
	private val KEY_CLOSE_ON_DENIAL = "KEY_CLOSE_ON_DENIAL"

	private var messageId = 0
	private var permission = ""
	private var requestId = 0
	private var closeAppOnDenial = true

	public constructor(fragmentManager: FragmentManager, messageId: Int, permission: String, requestId: Int, closeAppOnDenial: Boolean) : super()
	{
		val args = Bundle()
		args.putInt(KEY_MESSAGE_ID, messageId)
		args.putString(KEY_PERMISSION, permission)
		args.putBoolean(KEY_CLOSE_ON_DENIAL, closeAppOnDenial)
		args.putInt(KEY_REQUEST_PERMISSION_ID, requestId)

		this.arguments = args
		this.show(fragmentManager, TAG)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val args = this.arguments
		if (args != null)
		{
			this.messageId = args.getInt(KEY_MESSAGE_ID)
			this.permission = args.getString(KEY_PERMISSION)
			this.closeAppOnDenial = args.getBoolean(KEY_CLOSE_ON_DENIAL)
			this.requestId = args.getInt(KEY_REQUEST_PERMISSION_ID)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		val dialogBuilder = AlertDialog.Builder(this.activity)
		dialogBuilder.setTitle(R.string.request_permission_title)
		dialogBuilder.setMessage(this.messageId)

		if (this.closeAppOnDenial)
			dialogBuilder.setNegativeButton(R.string.dialog_close,
					{ dialogInterface, which -> this.activity?.finish() })
		else
			dialogBuilder.setNegativeButton(R.string.dialog_denial, null)

		dialogBuilder.setPositiveButton(R.string.dialog_grant, { dialogInterface, i -> this.requestPermission() })

		return dialogBuilder.create();
	}

	private fun requestPermission()
	{
		val activity = this.activity
		if (activity != null)
			ActivityCompat.requestPermissions(activity, arrayOf(this.permission), this.requestId) // TODO last paramter should be passed into constructor
		this.dismiss()
	}
}