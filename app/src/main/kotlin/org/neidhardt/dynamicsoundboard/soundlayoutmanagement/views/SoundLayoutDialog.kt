package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.EditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseDialog

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
abstract class SoundLayoutDialog : BaseDialog(), View.OnClickListener
{
	protected var soundLayoutName: EditText? = null
	protected var soundLayoutInput: TextInputLayout? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(this.getLayoutId(), null)
		this.soundLayoutName = view.findViewById(R.id.et_name_sound_layout) as EditText
		this.soundLayoutInput = (view.findViewById(R.id.ti_name_sound_layout) as TextInputLayout).apply { hint = getHintForName() }
		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		view.findViewById(R.id.b_ok).setOnClickListener(this)

		val dialog = AppCompatDialog(this.activity, R.style.DialogTheme)
        dialog.setTitle(this.getTitleId())
		dialog.setContentView(view)

		return dialog
	}

    protected abstract fun getTitleId(): Int

	protected abstract fun getLayoutId(): Int

	protected abstract fun getHintForName(): String?

	override fun onClick(v: View)
	{
		val id = v.id
		if (id == R.id.b_cancel)
			this.dismiss()
		else if (id == R.id.b_ok)
		{
			this.deliverResult()
			this.dismiss()
		}
	}

	protected abstract fun deliverResult()
}
