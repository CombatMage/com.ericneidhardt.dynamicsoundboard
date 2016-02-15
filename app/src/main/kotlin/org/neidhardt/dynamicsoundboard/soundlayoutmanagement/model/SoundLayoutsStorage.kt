package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
interface SoundLayoutsStorage
{
	fun getDbSoundLayouts(): DaoSession

	fun addSoundLayout(soundLayout: SoundLayout)

	fun removeSoundLayouts(soundLayoutsToRemove: List<SoundLayout>)
}