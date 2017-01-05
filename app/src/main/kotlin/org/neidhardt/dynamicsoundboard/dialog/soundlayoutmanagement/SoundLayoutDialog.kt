package org.neidhardt.dynamicsoundboard.dialog.soundlayoutmanagement

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.widget.EditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.base.BaseDialog

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
abstract class SoundLayoutDialog : BaseDialog()
{
	protected var soundLayoutName: EditText? = null
	protected var soundLayoutInput: TextInputLayout? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(this.getLayoutId(), null)
		this.soundLayoutName = view.findViewById(R.id.et_name_sound_layout) as EditText
		this.soundLayoutInput = (view.findViewById(R.id.ti_name_sound_layout) as TextInputLayout).apply { hint = getHintForName() }

		return AlertDialog.Builder(this.activity).apply {
			this.setTitle(getTitleId())
			this.setView(view)
			this.setNegativeButton(org.neidhardt.dynamicsoundboard.R.string.all_cancel, { dialogInterface, i -> dismiss() })
			this.setPositiveButton(getPositiveButtonId(), { dialogInterface, i -> deliverResult() })
		}.create()
	}

	protected abstract fun getPositiveButtonId(): Int

	protected abstract fun getTitleId(): Int

	protected abstract fun getLayoutId(): Int

	protected abstract fun getHintForName(): String?

	protected abstract fun deliverResult()
}