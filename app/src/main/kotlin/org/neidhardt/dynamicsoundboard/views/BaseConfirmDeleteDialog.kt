package org.neidhardt.dynamicsoundboard.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
abstract class BaseConfirmDeleteDialog : BaseDialog()
{

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
		val infoText = view.findViewById(R.id.tv_message) as TextView
		infoText.setText(this.infoTextResource)

		return AlertDialog.Builder(this.activity).apply {
			this.setView(view)
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss() })
			this.setPositiveButton(R.string.dialog_delete, { dialogInterface, i ->
				delete()
				dismiss() })
		}.create()
	}

	protected abstract val infoTextResource: Int

	protected abstract fun delete()
}
