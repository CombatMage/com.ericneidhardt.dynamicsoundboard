package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v4.content.ContextCompat
import com.emtronics.dragsortrecycler.DragSortRecycler
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication

/**
 * File created by eric.neidhardt on 23.04.2015.
 */
private val FLOATING_ITEM_ALPHA = 0.4f
private val FLOATING_ITEM_BG_COLOR_ID = R.color.accent_200
private val AUTO_SCROLL_SPEED = 0.3f
private val AUTO_SCROLL_WINDOW = 0.1f

internal class SoundDragSortRecycler(val dragViewId: Int) : DragSortRecycler()
{
	init
	{
		this.setViewHandleId(dragViewId)
		this.setFloatingAlpha(FLOATING_ITEM_ALPHA)
		this.setFloatingBgColor(ContextCompat.getColor(SoundboardApplication.context, FLOATING_ITEM_BG_COLOR_ID))
		this.setAutoScrollSpeed(AUTO_SCROLL_SPEED)
		this.setAutoScrollWindow(AUTO_SCROLL_WINDOW)
	}
}
