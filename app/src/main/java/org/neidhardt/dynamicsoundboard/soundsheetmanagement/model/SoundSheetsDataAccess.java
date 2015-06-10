package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public interface SoundSheetsDataAccess
{

	/**
	 * Retrieve all SoundSheets in the sound board.
	 * @return list of all SoundSheets
	 */
	List<SoundSheet> getSoundSheets();

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param position index of item to be selected
	 */
	void setSelectedItem(int position);

	/**
	 * Get the currently selected SoundSheet item.
	 */
	SoundSheet getSelectedItem();

	/**
	 * Get the SoundSheet item which corresponds to the given fragmentTag or null if no such items exists.
	 * @return SoundSheet or null
	 */
	SoundSheet getSoundSheetForFragmentTag(String fragmentTag);
}
