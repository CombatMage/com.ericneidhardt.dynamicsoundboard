package org.neidhardt.dynamicsoundboard.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 26.06.2015.
 */
public class DialogBaseLayout : LinearLayout
{
	private var hasRecyclerView = false
	private var hasTitle = true

	private var labelOk: String? = null
	private var labelCancel: String? = null

	public constructor(context: Context) : super(context) {}

	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.readAttributes(context, attrs)
	}

	public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		this.readAttributes(context, attrs)
	}

	private fun readAttributes(context: Context, attrs: AttributeSet)
	{
		var array = context.obtainStyledAttributes(attrs, R.styleable.DialogButtonBar, 0, 0)
		this.labelOk = array.getString(R.styleable.DialogButtonBar_text_ok_button) ?: context.getString(R.string.dialog_ok)
		this.labelCancel = array.getString(R.styleable.DialogButtonBar_text_cancle_button) ?: context.getString(R.string.dialog_cancel)
		array.recycle()

		array = context.obtainStyledAttributes(attrs, R.styleable.DialogBase, 0, 0)
		this.hasTitle = array.getBoolean(R.styleable.DialogBase_has_title, this.hasTitle)
		this.hasRecyclerView = array.getBoolean(R.styleable.DialogBase_has_recycler_view, this.hasRecyclerView)
		array.recycle()
	}

	override fun onFinishInflate()
	{
		super<LinearLayout>.onFinishInflate()

		val view = if (this.hasRecyclerView && this.hasTitle)
			LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_base_recycler_view_title, this, false)
		else if (this.hasRecyclerView)
			LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_base_recycler_view_no_title, this, false)
		else if (this.hasTitle)
			LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_base_title, this, false)
		else
			LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_base_no_title, this, false)

		(view.findViewById(R.id.b_cancel) as Button).setText(this.labelCancel)
		(view.findViewById(R.id.b_ok) as Button).setText(this.labelOk)

		val container = view.findViewById(R.id.layout_dialog_content) as ViewGroup
		while (0 < getChildCount())
		{
			val child = this.getChildAt(0)
			val params = child.getLayoutParams()

			removeViewAt(0);
			container.addView(child, params);
		}
		this.addView(view);
	}

	public fun enableRecyclerViewDividers(enable: Boolean)
	{
		if (this.hasRecyclerView)
		{
			val visibility = if (enable) View.VISIBLE else View.GONE
			this.findViewById(R.id.v_divider_top).setVisibility(visibility)
			this.findViewById(R.id.v_divider_bottom).setVisibility(visibility)
		}
	}
}
