package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import android.content.Context
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 31.01.2017.
 */
class PaddingDecorator(context: Context) : RecyclerView.ItemDecoration() {

	private val padding = context.resources.getDimensionPixelSize(R.dimen.margin_small)

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		val index = parent.getChildAdapterPosition(view)
		val isFirstItem = index == 0
		outRect.top = if (isFirstItem) padding else 0
	}
}