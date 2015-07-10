package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * File created by eric.neidhardt on 04.06.2015.
 */
public interface SoundSheetsDataStorage
{
	void addSoundSheetToManager(SoundSheet soundSheet);

	/**
	 * Remove the given {@code SoundSheet}s from the currently stored data set.
	 * @param soundSheets List of {@code SoundSheet}s to be removed
	 */
	void removeSoundSheets(List<SoundSheet> soundSheets);

	// TODO remove unused

	/**
	 * Get DaoSession used for storing {@code SoundSheet}
	 * @return corresponding {@code DaoSession}
	 */
	DaoSession getDbSoundSheets();

}
