package org.neidhardt.dynamicsoundboard.views

import android.app.DialogFragment
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
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
public abstract class BaseDialog : DialogFragment()
{
	companion object
	{
		protected val KEY_CALLING_FRAGMENT_TAG: String = "org.neidhardt.dynamicsoundboard.views.BaseDialog.callingFragmentTag"
	}

	protected var soundSheetsDataAccess: SoundSheetsDataAccess = DynamicSoundboardApplication.getSoundSheetsDataAccess()
	protected var soundSheetsDataStorage: SoundSheetsDataStorage = DynamicSoundboardApplication.getSoundSheetsDataStorage()
	protected var soundSheetsDataUtil: SoundSheetsDataUtil = DynamicSoundboardApplication.getSoundSheetsDataUtil()

	protected var soundsDataStorage: SoundsDataStorage = DynamicSoundboardApplication.getSoundsDataStorage()
	protected var soundsDataAccess: SoundsDataAccess = DynamicSoundboardApplication.getSoundsDataAccess()

	public var soundLayoutsAccess: SoundLayoutsAccess = DynamicSoundboardApplication.getSoundLayoutsAccess()

	public var mainView: DialogBaseLayout? = null

	public fun getSoundActivity(): SoundActivity
	{
		return this.activity as SoundActivity
	}
}
