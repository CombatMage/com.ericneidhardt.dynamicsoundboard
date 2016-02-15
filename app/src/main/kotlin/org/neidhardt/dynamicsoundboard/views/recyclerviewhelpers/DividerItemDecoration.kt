package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import org.neidhardt.dynamicsoundboard.R

class DividerItemDecoration(val context: Context) : RecyclerView.ItemDecoration()
{

	private val heightDivider: Int

	private val paintDivider: Paint
	private val paintBackground: Paint

	init
	{
		val colorBackground = ContextCompat.getColor(context, R.color.background)
		val colorDivider = ContextCompat.getColor(context, R.color.divider)

		this.heightDivider = context.resources.getDimensionPixelSize(R.dimen.stroke)

		this.paintDivider = Paint().apply {
			this.style = Paint.Style.FILL
			this.color = colorBackground
		}

		this.paintBackground = Paint().apply {
			this.style = Paint.Style.FILL
			this.color = colorDivider
		}
	}

	override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?)
	{
		if (parent.adapter == null)
			return

		val childCount = parent.adapter.itemCount
		if (childCount == 0)
			return

		for (i in 0..childCount - 1 - 1) // do not draw divider after last item
		{
			val child = parent.getChildAt(i) ?: continue

			val params = child.layoutParams as RecyclerView.LayoutParams
			val left = parent.paddingLeft
			val right = parent.width - parent.paddingRight
			val top = child.bottom + params.bottomMargin
			val bottom = top + this.heightDivider

			this.drawDividerBackground(canvas, left, top, right, bottom)
			this.drawDivider(canvas, left, top, right, bottom)
		}
	}

	private fun drawDividerBackground(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int)
	{
		canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), this.paintDivider)
	}

	private fun drawDivider(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int)
	{
		canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), this.paintBackground)
	}

	override fun getItemOffsets(outRect: Rect, childView: View, parent: RecyclerView, state: RecyclerView.State?)
	{
		val topOffset = 0
		val bottomOffset = this.heightDivider
		val rightOffset = 0
		val leftOffset = 0
		outRect.set(leftOffset, topOffset, rightOffset, bottomOffset)
	}
}

