package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public interface SoundSheetsDataModel
{
	/**
	 * Retrieve all SoundSheets in the sound board.
	 * @return list of all SoundSheets
	 */
	List<SoundSheet> getSoundSheets();
}
