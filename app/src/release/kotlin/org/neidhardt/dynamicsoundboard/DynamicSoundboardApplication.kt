package org.neidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManager;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsManager;

import java.util.Random;

public class DynamicSoundboardApplication : Application()
{
	companion object
	{
		private var applicationContext: Context? = null

		private val random = Random();

		private var soundLayoutsManager: SoundLayoutsManager? = null
		private var soundsManager: SoundsManager? = null
		private var soundSheetsManager: SoundSheetsManager? = null

		public fun getSoundsDataAccess(): SoundsDataAccess = this.soundsManager as SoundsDataAccess
		public fun getSoundsDataStorage(): SoundsDataStorage = this.soundsManager as SoundsDataStorage
		public fun getSoundsDataUtil(): SoundsDataUtil = this.soundsManager as SoundsDataUtil

		public fun getSoundSheetsDataAccess(): SoundSheetsDataAccess = this.soundSheetsManager as SoundSheetsDataAccess
		public fun getSoundSheetsDataStorage(): SoundSheetsDataStorage = this.soundSheetsManager as SoundSheetsDataStorage
		public fun getSoundSheetsDataUtil(): SoundSheetsDataUtil = this.soundSheetsManager as SoundSheetsDataUtil

		public fun getSoundLayoutsAccess(): SoundLayoutsAccess = this.soundLayoutsManager as SoundLayoutsAccess
		public fun getSoundLayoutsStorage(): SoundLayoutsStorage = this.soundLayoutsManager as SoundLayoutsStorage
		public fun getSoundLayoutsUtil(): SoundLayoutsUtil = this.soundLayoutsManager as SoundLayoutsUtil

		public fun getContext(): Context = applicationContext as Context

		public fun getRandomNumber(): Int = random.nextInt(Integer.MAX_VALUE)
	}

	override fun onCreate()
	{
		super.onCreate()

		applicationContext = this.getApplicationContext();

		soundLayoutsManager = SoundLayoutsManager()
		soundSheetsManager = SoundSheetsManager()
		soundsManager = SoundsManager()
	}
}
