package org.neidhardt.dynamicsoundboard.views.edittext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import org.neidhardt.dynamicsoundboard.R


class NoUnderscoreEditText(context: Context, attrs: AttributeSet) : CustomEditText(context, attrs)
{
	override var input: EditTextBackEvent? = null
	override var onTextEditedListener: CustomEditText.OnTextEditedListener? = null

	override fun inflateLayout(context: Context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_no_underscore_edittext, this, true)
		this.input = this.findViewById(R.id.edittext) as EditTextBackEvent
	}

}
