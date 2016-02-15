package org.neidhardt.dynamicsoundboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 26.06.2015.
 */
class DialogButtonBar : LinearLayout
{
	constructor(context: Context) : super(context)
	{
		this.init(context, null)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context, attrs)
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		this.init(context, attrs)
	}

	private fun init(context: Context, attrs: AttributeSet?)
	{
		LayoutInflater.from(context).inflate(R.layout.dialog_button_bar, this, true);

		if (attrs == null)
			return

		val array = context.obtainStyledAttributes(attrs, R.styleable.DialogButtonBar, 0, 0)

		(this.findViewById(R.id.b_cancel) as Button).text = array.getString(R.styleable.DialogButtonBar_text_cancle_button)
		(this.findViewById(R.id.b_ok) as Button).text = array.getString(R.styleable.DialogButtonBar_text_ok_button)
		array.recycle()
	}
}
