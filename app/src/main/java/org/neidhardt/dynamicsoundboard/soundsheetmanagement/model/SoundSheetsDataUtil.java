package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

/**
 * Created by eric.neidhardt on 02.06.2015.
 */
public interface SoundSheetsDataUtil
{
	/**
	 * Starts async task to retrieve sound sheets from cache.
	 */
	void init();

	/**
	 * Write back all existing SoundSheets to database.
	 */
	void writeCacheBack();

	/**
	 * Register the storage class on eventBus, should be called in onStart() of holding activity.
	 */
	void registerOnEventBus();

	/**
	 * Unregister the storage class on eventBus, should be called in onStop() of holding activity.
	 */
	void unregisterOnEventBus();
}
