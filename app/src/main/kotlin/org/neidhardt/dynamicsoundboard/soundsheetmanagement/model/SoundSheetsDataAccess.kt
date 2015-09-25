package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public interface SoundSheetsDataAccess
{

	/**
	 * Retrieve all SoundSheets in the sound board.
	 * @return list of all SoundSheets
	 */
	public fun getSoundSheets(): List<SoundSheet>

	/**
	 * Set the item with this position selected and all other items deselected
	 * @param soundSheetToSelect soundSheetToSelect to be selected
	 */
	public fun setSoundSheetSelected(soundSheetToSelect: SoundSheet)

	/**
	 * Get the currently selected SoundSheet item.
	 */
	public fun getSelectedItem(): SoundSheet?

	/**
	 * Get the SoundSheet item which corresponds to the given fragmentTag or null if no such items exists.
	 * @return SoundSheet or null
	 */
	public fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet?
}
