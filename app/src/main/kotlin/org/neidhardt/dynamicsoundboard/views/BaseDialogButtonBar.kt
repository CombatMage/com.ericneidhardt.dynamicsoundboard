package org.neidhardt.dynamicsoundboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.neidhardt.dynamicsoundboard.R

import kotlinx.android.synthetic.base_layout_dialog_button_bar

/**
 * File created by eric.neidhardt on 26.06.2015.
 */
class BaseDialogButtonBar : LinearLayout
{
	public constructor(context: Context) : super(context) {
	}

	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
	}

	public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
	}

	override fun onFinishInflate() {
		super.onFinishInflate()

		val view = LayoutInflater.from(this.getContext()).inflate(R.layout.base_layout_dialog_button_bar, this, false);
		// TODO
		/*
		while (0 < getChildCount())
		{
			View child = getChildAt(0);
			LinearLayout.MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
			removeViewAt(0);
			linearLayout.addView(child, layoutParams);
		}
		this.addView(view);*/
	}


}
