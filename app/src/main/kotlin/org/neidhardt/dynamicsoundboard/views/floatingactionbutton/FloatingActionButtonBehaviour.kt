package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 24.09.2015.
 */
public class FloatingActionButtonBehaviour(context : Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<AddPauseFloatingActionButton>(context, attrs)
{
	override fun layoutDependsOn(parent: CoordinatorLayout?, child: AddPauseFloatingActionButton?, dependency: View?): Boolean
	{
		return dependency != null && dependency.id == R.id.navigation_drawer_fragment
	}

	override fun onDependentViewChanged(parent: CoordinatorLayout?, child: AddPauseFloatingActionButton?, dependency: View): Boolean {
		if (child != null)
		{
			child.animateUiChanges()

			return true
		}
		else
			return super.onDependentViewChanged(parent, child, dependency)
	}
}