package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.View
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseDialog
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
public abstract class SoundLayoutDialog : BaseDialog(), View.OnClickListener
{
	protected var soundLayoutName: CustomEditText? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.getActivity().getLayoutInflater().inflate(this.getLayoutId(), null)
		this.soundLayoutName = view.findViewById(R.id.et_name_sound_layout) as CustomEditText
		this.soundLayoutName!!.setHint(this.getHintForName())

		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		view.findViewById(R.id.b_ok).setOnClickListener(this)

		val dialog = AppCompatDialog(this.getActivity(), R.style.DialogThemeNoTitle)
		dialog.setContentView(view)

		return dialog
	}

	protected abstract fun getLayoutId(): Int

	protected abstract fun getHintForName(): String

	override fun onClick(v: View)
	{
		val id = v.getId()
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
