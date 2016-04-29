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

		private val soundLayoutsManager: SoundLayoutsManager by lazy { SoundLayoutsManager(this.context) }
		private val soundsManager: SoundsManager by lazy { SoundsManager(this.context, this.soundLayoutsAccess, this.soundSheetsDataUtil) }
		private val soundSheetsManager: SoundSheetsManager by lazy { SoundSheetsManager(this.context, this.soundLayoutsAccess) }

		val soundsDataAccess: SoundsDataAccess get() = this.soundsManager
		val soundsDataStorage: SoundsDataStorage get() = this.soundsManager
		val soundsDataUtil: SoundsDataUtil get() = this.soundsManager

		val soundSheetsDataAccess: SoundSheetsDataAccess get() = this.soundSheetsManager
		val soundSheetsDataStorage: SoundSheetsDataStorage get() = this.soundSheetsManager
		val soundSheetsDataUtil: SoundSheetsDataUtil get() = this.soundSheetsManager

		val soundLayoutsAccess: SoundLayoutsAccess get() = this.soundLayoutsManager
		val soundLayoutsStorage: SoundLayoutsStorage get() = this.soundLayoutsManager
		val soundLayoutsUtil: SoundLayoutsUtil get() = this.soundLayoutsManager

		val randomNumber: Int get() = this.random.nextInt(Integer.MAX_VALUE)
	}

	override fun onCreate()
	{
		super.onCreate()
		staticContext = this.applicationContext
	}
}
