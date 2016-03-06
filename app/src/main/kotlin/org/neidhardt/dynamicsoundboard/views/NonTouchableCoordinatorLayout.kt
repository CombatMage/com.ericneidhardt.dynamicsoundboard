package org.neidhardt.dynamicsoundboard.views

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent

/**
* Created by ericn on 06.03.2016.
*/
class NonTouchableCoordinatorLayout : CoordinatorLayout
{
	var isScrollingEnabled: Boolean = true

	constructor(context: Context?) : super(context)

	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
		if (isScrollingEnabled)
			return super.onInterceptTouchEvent(ev)
		else
			return false
	}
}
