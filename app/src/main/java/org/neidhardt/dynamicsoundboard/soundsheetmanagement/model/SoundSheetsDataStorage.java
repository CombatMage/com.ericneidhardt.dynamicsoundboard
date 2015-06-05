package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * File created by eric.neidhardt on 04.06.2015.
 */
public interface SoundSheetsDataStorage
{

	/**
	 * Adds a new {@code SoundSheet} to Storage. If there is an existing {@code SoundSheet} with the same fragmentTag, it's label is
	 * updated.
	 * @param soundSheet new SoundSheet to store. Existing SoundSheets with the same fragmentTags are updated
	 * @return the fragmentTag of the added or update SoundSheet
	 */
	String addOrUpdateSoundSheet(SoundSheet soundSheet);

	/**
	 * Remove the given {@code SoundSheet} from the currently stored data set.
	 * @param soundSheet {@code SoundSheet} to be removed
	 */
	void removeSoundSheet(SoundSheet soundSheet);

	/**
	 * Remove all currently stored SoundSheets from the data set.
	 */
	void removeAllSoundSheets();
}
