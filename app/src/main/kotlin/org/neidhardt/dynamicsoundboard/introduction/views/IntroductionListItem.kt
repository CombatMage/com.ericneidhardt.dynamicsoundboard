package org.neidhardt.dynamicsoundboard.introduction.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R

/**
* File created by eric.neidhardt on 06.02.2015.
*/
class IntroductionListItem : RelativeLayout {
	private var icon: ImageView? = null
	private var title: TextView? = null
	private var summary: TextView? = null

	constructor(context: Context) : super(context) {
		this.init(context)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.init(context)
		this.readAttributes(context, attrs)
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.init(context)
		this.readAttributes(context, attrs)
	}

	private fun init(context: Context) {
		LayoutInflater.from(context).inflate(R.layout.view_introduction_list_item, this, true)
		this.icon = this.findViewById(R.id.iv_introduction_icon) as ImageView
		this.title = this.findViewById(R.id.tv_introduction_title) as TextView
		this.summary = this.findViewById(R.id.tv_introduction_summary) as TextView
	}

	private fun readAttributes(context: Context, attributeSet: AttributeSet) {
		val array = context.obtainStyledAttributes(attributeSet, R.styleable.IntroductionListItem, 0, 0)
		val icon = array.getDrawable(R.styleable.IntroductionListItem_action_icon)
		if (icon != null)
			this.icon!!.setImageDrawable(icon)

		val title = array.getString(R.styleable.IntroductionListItem_action_title)
		if (title != null)
			this.title!!.text = title

		val summary = array.getString(R.styleable.IntroductionListItem_action_summary)
		if (title != null)
			this.summary!!.text = summary

		array.recycle()
	}

}
