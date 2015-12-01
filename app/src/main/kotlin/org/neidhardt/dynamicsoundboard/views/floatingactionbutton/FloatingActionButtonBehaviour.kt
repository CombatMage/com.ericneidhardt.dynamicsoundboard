package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionButton

/**
 * File created by eric.neidhardt on 24.09.2015.
 */
class FloatingActionButtonBehaviour(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs)
{
	private val SCROLL_THRESHOLD = 4

	override fun layoutDependsOn(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean
			= super.layoutDependsOn(parent, fab, dependency) || (dependency is RecyclerView)

	override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
									 fab: FloatingActionButton,
									 directTargetChild: View,
									 target: View,
									 nestedScrollAxes: Int): Boolean
		= nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL && target is RecyclerView

	override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
								fab: FloatingActionButton,
								target: View,
								dxConsumed: Int,
								dyConsumed: Int,
								dxUnconsumed: Int,
								dyUnconsumed: Int)
    {
		if (target is RecyclerView)
		{
			if (Math.abs(dyConsumed) > SCROLL_THRESHOLD)
			{
				if (dyConsumed > 0)
					fab.hide(true)
				else
					fab.show(true)
			}
		} else
			super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
	}
}