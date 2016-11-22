package org.neidhardt.dynamicsoundboard.views.edittext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_no_underscore_edittext.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.ui_utils.views.CustomEditText
import org.neidhardt.ui_utils.views.EditTextBackEvent


class NoUnderscoreEditText(context: Context, attrs: AttributeSet) : CustomEditText(context, attrs) {

	private var inputField: EditTextBackEvent? = null
	override var input: EditTextBackEvent
		get() = inputField as EditTextBackEvent
		set(value) { this.inputField = value }

	override var mOnTextEditedListener: OnTextEditedListener? = null

	override fun inflateLayout(context: Context) {
		LayoutInflater.from(context).inflate(R.layout.view_no_underscore_edittext, this, true)
		this.input = this.et_view_no_underscore_edittext
	}
}
