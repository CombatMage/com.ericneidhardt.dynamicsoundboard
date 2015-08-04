package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.dao.SoundSheet

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

	public fun getSoundLayouts(): List<SoundLayout>
}
