package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.Toast

/**
 * File created by eric.neidhardt on 24.09.2015.
 */
@SuppressWarnings("unused")
public class FloatingActionButtonBehaviour(context : Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<AddPauseFloatingActionButton>(context, attrs)
{
	public override fun layoutDependsOn(parent: CoordinatorLayout, fab: AddPauseFloatingActionButton, dependency: View) : Boolean
	{
		return super.layoutDependsOn(parent, fab, dependency) || (dependency is RecyclerView)
	}

	public override fun onDependentViewChanged(parent: CoordinatorLayout, fab: AddPauseFloatingActionButton, dependency: View) : Boolean
	{
		val returnValue = super.onDependentViewChanged(parent, fab, dependency)
		if (dependency is RecyclerView)
		{
			Toast.makeText(parent.context, "test", Toast.LENGTH_SHORT).show()
		}
		return returnValue
	}
}