package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundsDataUtil
{
	/**
	 * Starts async task to retrieve sounds from database.
	 */
	void init();

	/**
	 * Write back all existing sounds and the playlist to database.
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
