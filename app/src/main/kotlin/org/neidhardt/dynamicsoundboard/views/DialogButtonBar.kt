package org.neidhardt.dynamicsoundboard.views

import org.neidhardt.dynamicsoundboard.R
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * File created by eric.neidhardt on 26.06.2015.
 */
class DialogButtonBar : LinearLayout
{
	public constructor(context: Context) : super(context)
	{
		this.init(context, null)
	}

	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context, attrs)
	}

	public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		this.init(context, attrs)
	}

	private fun init(context: Context, attrs: AttributeSet?)
	{
		LayoutInflater.from(context).inflate(R.layout.dialog_button_bar, this, true);

		if (attrs == null)
			return

		val array = context.obtainStyledAttributes(attrs, R.styleable.DialogButtonBar, 0, 0)

		(this.findViewById(R.id.b_cancel) as Button).setText(array.getString(R.styleable.DialogButtonBar_text_cancle_button))
		(this.findViewById(R.id.b_ok) as Button).setText(array.getString(R.styleable.DialogButtonBar_text_ok_button))
		array.recycle()
	}
}
