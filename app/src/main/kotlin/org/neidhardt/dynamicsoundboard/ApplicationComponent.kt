package org.neidhardt.dynamicsoundboard

import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManager
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsManager

/**
 * File created by eric.neidhardt on 09.07.2015.
 */
public class ApplicationComponent()
{
	private val soundsManager = SoundsManager()
	private val soundSheetsManager = SoundSheetsManager()

	public val soundsDataAccess: SoundsDataAccess = this.soundsManager
	public val soundsDataStorage: SoundsDataStorage = this.soundsManager
	public val soundsDataUtil: SoundsDataUtil = this.soundsManager

	public val soundSheetsDataAccess: SoundSheetsDataAccess = this.soundSheetsManager
	public val soundSheetsDataStorage: SoundSheetsDataStorage = this.soundSheetsManager
	public val soundSheetsDataUtil: SoundSheetsDataUtil = this.soundSheetsManager
}