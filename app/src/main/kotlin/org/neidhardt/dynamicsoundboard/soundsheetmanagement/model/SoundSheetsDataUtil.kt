package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundSheetsDataUtil
{
	/**
	 * Starts async task to retrieve sound sheets from database. If `SoundSheetsDataUtil` was initialized before,
	 * the corresponding events are fired, but no database transaction is performed.
	 * @return true if init was required and async handling has started, else false.
	 */
	public fun initIfRequired(): Boolean

	/**
	 * Generate a new SoundSheet instance with unique fragmentTag, derived from the given label.
	 * @param label label of the new `SoundSheet`
	 * *
	 * @return new `SoundSheet` instance
	 */
	public fun getNewSoundSheet(label: String): SoundSheet

	/**
	 * Get suggested SoundSheet name to create a new SoundSheets
	 */
	public fun getSuggestedName(): String

	public fun isPlaylistSoundSheet(fragmentTag: String): Boolean
}
