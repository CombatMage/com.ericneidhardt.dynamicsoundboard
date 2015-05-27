package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public interface SoundLayoutModel
{
	/**
	 * Retrieve the currently selected SoundLayout.
	 * @return currently active SoundLayout
	 */
	SoundLayout getActiveSoundLayout();
}
