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
	 */
	void init();

	/**
	 * Check if {@code SoundSheetsDataUtil.init()} was called.
	 * @return true if {@code SoundSheetsDataUtil} is init, else false.
	 */
	boolean isInit();

	/**
	 * Register the storage class on eventBus, should be called in onStart() of holding activity.
	 */
	void registerOnEventBus();

	/**
	 * Unregister the storage class on eventBus, should be called in onStop() of holding activity.
	 */
	void unregisterOnEventBus();

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
