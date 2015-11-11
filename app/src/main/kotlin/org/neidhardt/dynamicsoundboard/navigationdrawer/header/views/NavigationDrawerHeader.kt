package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
class NavigationDrawerHeader : FrameLayout, View.OnClickListener
{
	private val presenter = NavigationDrawerHeaderPresenter(DynamicSoundboardApplication.getSoundLayoutsAccess())

	private var currentLayoutName: TextView? = null
	private var indicator: View? = null

	@SuppressWarnings("unused")
	constructor(context: Context) : super(context) {
		this.init(context)
	}

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.init(context)
	}

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.init(context)
	}

	private fun init(context: Context) {
		LayoutInflater.from(context).inflate(R.layout.navigation_drawer_header, this, true)
		this.currentLayoutName = this.findViewById(R.id.tv_current_sound_layout_name) as TextView
		this.indicator = this.findViewById(R.id.iv_change_sound_layout_indicator)

		this.findViewById(R.id.layout_change_sound_layout).setOnClickListener(this)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter.onAttachedToWindow()
	}

	override fun onDetachedFromWindow() {
		this.presenter.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter.view = this
	}

	override fun onClick(v: View) {
		this.presenter.onChangeLayoutClicked()
	}

	internal fun showCurrentLayoutName(name: String) {
		this.currentLayoutName!!.text = name
	}

	internal fun animateLayoutChanges() {
		this.indicator!!.animate().rotationXBy(180f).setDuration(this.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()).start()
	}
}
