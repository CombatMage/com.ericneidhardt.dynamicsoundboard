package org.neidhardt.dynamicsoundboard.views.toolbarhelpers

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet

/**
 * File created by eric.neidhardt on 22.03.2016.
 */
enum class State {
	COLLAPSED,
	EXPANDED,
	COLLAPSING,
	EXPANDING,
	IDLE
}

class ImprovedAppBarLayout : AppBarLayout, AppBarLayout.OnOffsetChangedListener {

	var onStateChangeListener: OnStateChangeListener? = null

	val totalVerticalScroll: Int
		get() = this.totalScrollRange

	private var state: State = State.IDLE

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (!(layoutParams is CoordinatorLayout.LayoutParams)
				|| !(parent is CoordinatorLayout)) {
			throw  IllegalStateException("ImprovedAppBarLayout must be a direct child of CoordinatorLayout.");
		}
		addOnOffsetChangedListener(this);
	}

	override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
		if (verticalOffset == 0) {
			if (state != State.EXPANDED)
				this.onStateChangeListener?.onStateChange(State.EXPANDED, verticalOffset)
			this.state = State.EXPANDED;
		}
		else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
			if (this.state != State.COLLAPSED)
				this.onStateChangeListener?.onStateChange(State.COLLAPSED, verticalOffset)
			this.state = State.COLLAPSED;
		}
		else if (this.state == State.COLLAPSED)
		{
			onStateChangeListener?.onStateChange(State.EXPANDING, verticalOffset)
			this.state == State.EXPANDING
		}
		else if (this.state == State.EXPANDED)
		{
			onStateChangeListener?.onStateChange(State.COLLAPSING, verticalOffset)
			this.state == State.COLLAPSING
		}
		else {
			onStateChangeListener?.onStateChange(State.IDLE, verticalOffset)
			this.state == State.IDLE
		}
	}

	interface OnStateChangeListener
	{
		fun onStateChange(toolbarChange: State, verticalOffset: Int)
	}
}