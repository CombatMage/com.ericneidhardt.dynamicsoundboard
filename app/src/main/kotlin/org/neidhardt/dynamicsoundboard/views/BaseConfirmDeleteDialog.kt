package org.neidhardt.dynamicsoundboard.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
abstract class BaseConfirmDeleteDialog : BaseDialog(), View.OnClickListener
{

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
		val infoText = view.findViewById(R.id.tv_message) as TextView
		infoText.setText(this.infoTextResource)

		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		view.findViewById(R.id.b_ok).setOnClickListener(this)

		val dialog = AppCompatDialog(this.activity, R.style.DialogThemeNoTitle)
		dialog.setContentView(view)
		return dialog
	}

	override fun onClick(v: View)
	{
		when (v.id) {
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok -> {
				this.delete()
				this.dismiss()
			}
		}
	}

	protected abstract val infoTextResource: Int

	protected abstract fun delete()
}
