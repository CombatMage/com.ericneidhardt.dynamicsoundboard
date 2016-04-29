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

	protected var soundSheetsDataAccess: SoundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess
	protected var soundSheetsDataStorage: SoundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	protected var soundSheetsDataUtil: SoundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil

	protected var soundsDataStorage: SoundsDataStorage = SoundboardApplication.soundsDataStorage
	protected var soundsDataAccess: SoundsDataAccess = SoundboardApplication.soundsDataAccess

	var soundLayoutsAccess: SoundLayoutsAccess = SoundboardApplication.soundLayoutsAccess

	val soundActivity: SoundActivity
		get() = this.activity as SoundActivity
}

