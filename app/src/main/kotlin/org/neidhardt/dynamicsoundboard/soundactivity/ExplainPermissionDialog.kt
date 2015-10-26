package org.neidhardt.dynamicsoundboard.soundactivity

import android.app.Dialog
import android.app.FragmentManager
import android.os.Bundle
import android.app.AlertDialog
import org.neidhardt.dynamicsoundboard.views.BaseDialog

/**
 * File created by eric.neidhardt on 22.10.2015.
 */
public class ExplainPermissionDialog : BaseDialog
{
	private val TAG = javaClass.name

	private val KEY_MESSAGE_ID = "KEY_MESSAGE_ID"
	private val KEY_PERMISSION = "KEY_PERMISSION"
	private val KEY_CLOSE_ON_DENIAL = "KEY_CLOSE_ON_DENIAL"

	private var messageId = 0
	private var permission = ""
	private var closeAppOnDenial = true

	public constructor(fragmentManager: FragmentManager, messageId: Int, permission: String, closeAppOnDenial: Boolean) : super()
	{
		val args = Bundle()
		args.putInt(KEY_MESSAGE_ID, messageId)
		args.putString(KEY_PERMISSION, permission)
		args.putBoolean(KEY_CLOSE_ON_DENIAL, closeAppOnDenial)

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
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		val dialogBuilder = AlertDialog.Builder(this.activity)
		dialogBuilder.setMessage(this.messageId)



		return dialogBuilder.create();
	}
}