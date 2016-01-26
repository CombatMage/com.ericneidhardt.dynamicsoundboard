package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ScrollingView
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionButton

/**
 * File created by eric.neidhardt on 24.09.2015.
 */
private val SCROLL_THRESHOLD = 4

interface SnackbarBehaviour
{
	fun layoutDependsOn(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean
			= (dependency is Snackbar.SnackbarLayout)

	fun onDependentViewChanged(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean
	{
		val translationY = Math.min(0f, dependency.translationY - dependency.height)
		fab.translationY = translationY;
		return true;
	}
}

interface ScrollViewBehaviour
{
	fun layoutDependsOn(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean
			= (dependency is ScrollingView)

	fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
							fab: FloatingActionButton,
							directTargetChild: View,
							target: View,
							nestedScrollAxes: Int): Boolean
			= nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL && target is ScrollingView

	fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
					   fab: FloatingActionButton,
					   target: View,
					   dxConsumed: Int,
					   dyConsumed: Int,
					   dxUnconsumed: Int,
					   dyUnconsumed: Int)
	{
		if (target is ScrollingView)
		{
			if (Math.abs(dyConsumed) > SCROLL_THRESHOLD)
			{
				if (dyConsumed > 0)
					fab.hide(true)
				else
					fab.show(true)
			}
		}
	}
}

class FloatingActionButtonBehaviour(context: Context, attrs: AttributeSet) :
		CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs),
		ScrollViewBehaviour,
		SnackbarBehaviour
{
	override fun layoutDependsOn(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean =
			super<CoordinatorLayout.Behavior>.layoutDependsOn(parent, fab, dependency)
			|| super<ScrollViewBehaviour>.layoutDependsOn(parent, fab, dependency)
			|| super<SnackbarBehaviour>.layoutDependsOn(parent, fab, dependency)

	override fun onDependentViewChanged(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean =
			super<CoordinatorLayout.Behavior>.layoutDependsOn(parent, fab, dependency)
			|| super<SnackbarBehaviour>.onDependentViewChanged(parent, fab, dependency)

	override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
									 fab: FloatingActionButton,
									 directTargetChild: View,
									 target: View,
									 nestedScrollAxes: Int): Boolean
		= super<ScrollViewBehaviour>.onStartNestedScroll(coordinatorLayout, fab, directTargetChild, target, nestedScrollAxes)

	override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
								fab: FloatingActionButton,
								target: View,
								dxConsumed: Int,
								dyConsumed: Int,
								dxUnconsumed: Int,
								dyUnconsumed: Int)
		= super<ScrollViewBehaviour>.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
}