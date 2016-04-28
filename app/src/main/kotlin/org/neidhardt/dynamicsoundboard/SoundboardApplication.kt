package org.neidhardt.dynamicsoundboard;

import android.app.Application
import android.content.Context
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManager
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsManager
import java.util.*


open class SoundboardApplication : Application()
{

	companion object
	{
		private var staticContext: Context? = null
		val context: Context
			get() = this.staticContext as Context

		private val random = Random()

		private var soundLayoutsManager: SoundLayoutsManager? = null
		private var soundsManager: SoundsManager? = null
		private var soundSheetsManager: SoundSheetsManager? = null

		fun getSoundsDataAccess(): SoundsDataAccess = this.soundsManager as SoundsDataAccess
		fun getSoundsDataStorage(): SoundsDataStorage = this.soundsManager as SoundsDataStorage
		fun getSoundsDataUtil(): SoundsDataUtil = this.soundsManager as SoundsDataUtil

		fun getSoundSheetsDataAccess(): SoundSheetsDataAccess = this.soundSheetsManager as SoundSheetsDataAccess
		fun getSoundSheetsDataStorage(): SoundSheetsDataStorage = this.soundSheetsManager as SoundSheetsDataStorage
		fun getSoundSheetsDataUtil(): SoundSheetsDataUtil = this.soundSheetsManager as SoundSheetsDataUtil

		fun getSoundLayoutsAccess(): SoundLayoutsAccess = this.soundLayoutsManager as SoundLayoutsAccess
		fun getSoundLayoutsStorage(): SoundLayoutsStorage = this.soundLayoutsManager as SoundLayoutsStorage
		fun getSoundLayoutsUtil(): SoundLayoutsUtil = this.soundLayoutsManager as SoundLayoutsUtil

		val randomNumber: Int get() = random.nextInt(Integer.MAX_VALUE)
	}

	override fun onCreate()
	{
		super.onCreate()

		staticContext = this.applicationContext
		soundLayoutsManager = SoundLayoutsManager()
		soundSheetsManager = SoundSheetsManager()
		soundsManager = SoundsManager()
	}
}
