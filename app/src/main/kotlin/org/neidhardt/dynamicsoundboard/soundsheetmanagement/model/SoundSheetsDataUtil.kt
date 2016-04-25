package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
interface SoundSheetsDataUtil
{
	/**
	 * Starts async task to retrieve sound sheets from database. If `SoundSheetsDataUtil` was initialized before,
	 * the corresponding events are fired, but no database transaction is performed.
	 * @return true if init was required and async handling has started, else false.
	 */
	fun initIfRequired(): Boolean

	/**
	 * Release all existing `SoundSheet` and set state to not init.
	 */
	fun releaseAll()

	/**
	 * Generate a new SoundSheet instance with unique fragmentTag, derived from the given label.
	 * @param label label of the new `SoundSheet`
	 * *
	 * @return new `SoundSheet` instance
	 */
	fun getNewSoundSheet(label: String): SoundSheet

	/**
	 * Get suggested SoundSheet name to create a new SoundSheets
	 */
	fun getSuggestedName(): String

	fun isPlaylistSoundSheet(fragmentTag: String): Boolean
}
