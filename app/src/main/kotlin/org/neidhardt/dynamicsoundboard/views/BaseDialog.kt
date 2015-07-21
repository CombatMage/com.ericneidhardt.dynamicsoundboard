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
	protected var soundSheetsDataAccess: SoundSheetsDataAccess = DynamicSoundboardApplication.getStorage().soundSheetsDataAccess
	protected var soundSheetsDataStorage: SoundSheetsDataStorage = DynamicSoundboardApplication.getStorage().soundSheetsDataStorage
	protected var soundSheetsDataUtil: SoundSheetsDataUtil = DynamicSoundboardApplication.getStorage().soundSheetsDataUtil

	protected var soundsDataStorage: SoundsDataStorage = DynamicSoundboardApplication.getStorage().soundsDataStorage
	protected var soundsDataAccess: SoundsDataAccess = DynamicSoundboardApplication.getStorage().soundsDataAccess

	public var soundLayoutsAccess: SoundLayoutsAccess = DynamicSoundboardApplication.getStorage().soundLayoutsAccess

	public var mainView: DialogBaseLayout? = null

	public fun getSoundActivity(): SoundActivity
	{
		return this.getActivity() as SoundActivity
	}
}
