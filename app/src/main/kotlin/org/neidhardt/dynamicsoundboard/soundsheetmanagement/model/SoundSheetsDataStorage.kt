package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 04.06.2015.
 */
interface SoundSheetsDataStorage
{
	fun addSoundSheetToManager(soundSheet: SoundSheet)

	/**
	 * Remove the given `SoundSheet`s from the currently stored data set.
	 * @param soundSheets List of `SoundSheet`s to be removed
	 */
	fun removeSoundSheets(soundSheets: List<SoundSheet>)

	/**
	 * Get DaoSession used for storing `SoundSheet`
	 * @return corresponding `DaoSession`
	 */
	fun getDbSoundSheets(): DaoSession

}
