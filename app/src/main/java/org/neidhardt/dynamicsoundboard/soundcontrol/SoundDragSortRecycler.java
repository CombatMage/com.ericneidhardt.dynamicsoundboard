package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.support.v4.content.ContextCompat;
import com.emtronics.dragsortrecycler.DragSortRecycler;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;

/**
 * File created by eric.neidhardt on 23.04.2015.
 */
class SoundDragSortRecycler extends DragSortRecycler
{
	private static final float FLOATING_ITEM_ALPHA = 0.4f;
	private static final int FLOATING_ITEM_BG_COLOR_ID = R.color.accent_200;
	private static final float AUTO_SCROLL_SPEED = 0.3f;
	private static final float AUTO_SCROLL_WINDOW = 0.1f;

	private final int dragViewId;

	public SoundDragSortRecycler(int dragViewId)
	{
		this.dragViewId = dragViewId;

		this.setViewHandleId(dragViewId);
		this.setFloatingAlpha(FLOATING_ITEM_ALPHA);
		this.setFloatingBgColor(ContextCompat.getColor(DynamicSoundboardApplication.Companion.getContext(), FLOATING_ITEM_BG_COLOR_ID));
		this.setAutoScrollSpeed(AUTO_SCROLL_SPEED);
		this.setAutoScrollWindow(AUTO_SCROLL_WINDOW);
	}

	public int getDragViewId()
	{
		return dragViewId;
	}
}
