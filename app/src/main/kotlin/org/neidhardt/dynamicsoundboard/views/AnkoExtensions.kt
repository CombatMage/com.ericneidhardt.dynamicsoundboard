package org.neidhardt.dynamicsoundboard.views

import android.support.annotation.AttrRes
import android.util.TypedValue
import org.jetbrains.anko.AnkoContext

/**
 * Created by eric.neidhardt@gmail.com on 27.01.2017.
 */
val <T> AnkoContext<T>.selectableItemBackgroundResource: Int get() {
	return this.getResourceIdAttribute(android.R.attr.selectableItemBackground)
}

val <T> AnkoContext<T>.selectableItemBackgroundBorderlessResource: Int get() {
	return this.getResourceIdAttribute(android.R.attr.selectableItemBackgroundBorderless)
}

fun <T> AnkoContext<T>.getResourceIdAttribute(@AttrRes attribute: Int) : Int {
	val typedValue = TypedValue()
	this.ctx.theme.resolveAttribute(attribute, typedValue, true)
	return typedValue.resourceId
}