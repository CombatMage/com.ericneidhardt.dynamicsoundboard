package org.neidhardt.dynamicsoundboard.views.edittext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_no_underscore_edittext.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.ui_utils.views.CustomEditText
import org.neidhardt.ui_utils.views.EditTextBackEvent
import kotlin.properties.Delegates


class NoUnderscoreEditText(context: Context, attrs: AttributeSet) : CustomEditText(context, attrs) {

	override var input: EditTextBackEvent by Delegates.notNull<EditTextBackEvent>()
	override var onTextEditedListener: OnTextEditedListener? = null

	override fun inflateLayout(context: Context) {
		LayoutInflater.from(context).inflate(R.layout.view_no_underscore_edittext, this, true)
		this.input = this.et_view_no_underscore_edittext
	}
}
