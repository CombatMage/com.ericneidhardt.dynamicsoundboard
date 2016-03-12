package org.neidhardt.dynamicsoundboard.views

import android.support.v4.app.DialogFragment
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
abstract class BaseDialog : DialogFragment()
{
	protected val KEY_CALLING_FRAGMENT_TAG: String = "KEY_CALLING_FRAGMENT_TAG"

	protected var soundSheetsDataAccess: SoundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess()
	protected var soundSheetsDataStorage: SoundSheetsDataStorage = SoundboardApplication.getSoundSheetsDataStorage()
	protected var soundSheetsDataUtil: SoundSheetsDataUtil = SoundboardApplication.getSoundSheetsDataUtil()

	protected var soundsDataStorage: SoundsDataStorage = SoundboardApplication.getSoundsDataStorage()
	protected var soundsDataAccess: SoundsDataAccess = SoundboardApplication.getSoundsDataAccess()

	var soundLayoutsAccess: SoundLayoutsAccess = SoundboardApplication.getSoundLayoutsAccess()

	var mainView: DialogBaseLayout? = null

	val soundActivity: SoundActivity
		get() = this.activity as SoundActivity
}

