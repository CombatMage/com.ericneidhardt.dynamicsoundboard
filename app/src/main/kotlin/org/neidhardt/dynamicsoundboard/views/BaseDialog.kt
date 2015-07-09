package org.neidhardt.dynamicsoundboard.views

import android.app.DialogFragment
import android.os.Bundle
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity
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
	protected var soundSheetsDataAccess: SoundSheetsDataAccess = DynamicSoundboardApplication.getApplicationComponent().soundSheetsDataAccess
	protected var soundSheetsDataStorage: SoundSheetsDataStorage = DynamicSoundboardApplication.getApplicationComponent().soundSheetsDataStorage
	protected var soundSheetsDataUtil: SoundSheetsDataUtil = DynamicSoundboardApplication.getApplicationComponent().soundSheetsDataUtil

	protected var soundsDataStorage: SoundsDataStorage = DynamicSoundboardApplication.getApplicationComponent().soundsDataStorage
	protected var soundsDataAccess: SoundsDataAccess = DynamicSoundboardApplication.getApplicationComponent().soundsDataAccess

	public var mainView: DialogBaseLayout? = null

	public fun getSoundActivity(): SoundActivity
	{
		return this.getActivity() as SoundActivity
	}
}
