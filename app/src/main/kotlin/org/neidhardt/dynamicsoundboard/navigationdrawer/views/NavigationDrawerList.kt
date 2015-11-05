package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout


abstract class NavigationDrawerList : FrameLayout
{
	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	abstract val itemCount: Int

	abstract val actionModeTitle: Int

}
