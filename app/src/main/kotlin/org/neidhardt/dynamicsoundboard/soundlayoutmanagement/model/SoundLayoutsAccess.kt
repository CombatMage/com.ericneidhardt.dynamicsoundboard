package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
interface SoundLayoutsAccess
{
	/**
	 * Retrieve the currently selected SoundLayout.
	 * @return currently active SoundLayout
	 */
	public fun getActiveSoundLayout(): SoundLayout

	public fun getSoundLayoutById(databaseId: String): SoundLayout?

	public fun setSoundLayoutSelected(position: Int)
}
