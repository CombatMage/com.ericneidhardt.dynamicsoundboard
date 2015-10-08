package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 24.09.2015.
 */
@SuppressWarnings("unused")
public class FloatingActionButtonBehaviour(context : Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<AddPauseFloatingActionButton>(context, attrs)
{
	private val toolbarHeight = context.resources.getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material)

	public override fun layoutDependsOn(parent: CoordinatorLayout, fab: AddPauseFloatingActionButton, dependency: View) : Boolean
	{
		return super.layoutDependsOn(parent, fab, dependency) || (dependency is AppBarLayout)
	}

	public override fun onDependentViewChanged(parent: CoordinatorLayout, fab: AddPauseFloatingActionButton, dependency: View) : Boolean
	{
		val returnValue = super.onDependentViewChanged(parent, fab, dependency)
		if (dependency is AppBarLayout)
		{
			val params = fab.layoutParams as CoordinatorLayout.LayoutParams
			val fabBottomMargin = params.bottomMargin
			val distanceToScroll = fab.height + fabBottomMargin
			val ratio = dependency.y.toFloat() / toolbarHeight.toFloat()
			fab.translationY = -distanceToScroll * ratio
		}
		return returnValue
	}
}

@SuppressWarnings("unused")
public class FloatingActionButtonBehaviour2(context : Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<AddPauseFloatingActionButton>(context, attrs)
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