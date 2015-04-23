package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.content.res.Resources;
import com.emtronics.dragsortrecycler.DragSortRecycler;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 23.04.2015.
 */
class SoundSortRecycler extends DragSortRecycler
{
	private static final float FLOATING_ITEM_ALPHA = 0.4f;
	private static final int FLOATING_ITEM_BG_COLOR_ID = R.color.accent_200;
	private static final float AUTO_SCROLL_SPEED = 0.3f;
	private static final float AUTO_SCROLL_WINDOW = 0.1f;

	private final int dragViewId;

	public SoundSortRecycler(Resources resources, int dragViewId)
	{
		this.dragViewId = dragViewId;

		this.setViewHandleId(dragViewId);
		this.setFloatingAlpha(FLOATING_ITEM_ALPHA);
		this.setFloatingBgColor(resources.getColor(FLOATING_ITEM_BG_COLOR_ID));
		this.setAutoScrollSpeed(AUTO_SCROLL_SPEED);
		this.setAutoScrollWindow(AUTO_SCROLL_WINDOW);
	}

	public int getDragViewId()
	{
		return dragViewId;
	}
}
