package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
interface SoundSheetsDataAccess
{

	/**
	 * Retrieve all SoundSheets in the sound board.
	 * @return list of all SoundSheets
	 */
	fun getSoundSheets(): List<SoundSheet>

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param soundSheetToSelect soundSheetToSelect to be selected
	 */
	fun setSoundSheetSelected(soundSheetToSelect: SoundSheet)

	/**
	 * Get the currently selected SoundSheet item.
	 */
	fun getSelectedItem(): SoundSheet?

	/**
	 * Get the SoundSheet item which corresponds to the given fragmentTag or null if no such items exists.
	 * @return SoundSheet or null
	 */
	fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet?
}
