package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundSheetsDataUtil
{
	/**
	 * Starts async task to retrieve sound sheets from database. If {@code SoundSheetsDataUtil} was initialized before,
	 * the corresponding events are fired, but no database transaction is performed.
	 * @return true if init was required and async handling has started, else false.
	 */
	boolean initIfRequired();

	/**
	 * Check if {@code SoundSheetsDataUtil.initIfRequired()} was called.
	 * @return true if {@code SoundSheetsDataUtil} is initIfRequired, else false.
	 */
	boolean isInit();

	/**
	 * Generate a new SoundSheet instance with unique fragmentTag, derived from the given label.
	 * @param label label of the new {@code SoundSheet}
	 * @return new {@code SoundSheet} instance
	 */
	SoundSheet getNewSoundSheet(String label);

	/**
	 * Get suggested SoundSheet name to create a new SoundSheets
	 */
	String getSuggestedName();
}
