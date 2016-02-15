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
	fun getActiveSoundLayout(): SoundLayout

	fun getSoundLayoutById(databaseId: String): SoundLayout?

	fun setSoundLayoutSelected(position: Int)

	fun getSoundLayouts(): List<SoundLayout>
}
