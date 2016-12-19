package org.neidhardt.dynamicsoundboard

import android.content.Context
import android.support.multidex.MultiDexApplication
import org.neidhardt.dynamicsoundboard.manager.NewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundSheetManager
import org.neidhardt.dynamicsoundboard.persistance.AppDataStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManager
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsManager
import org.neidhardt.utils.ValueHolder
import java.util.*


open class SoundboardApplication : MultiDexApplication() {

	companion object {

		private var staticContext: Context? = null
		val context: Context get() = this.staticContext as Context

		private val random = Random()

		private val mSoundLayoutsManager: SoundLayoutManager by lazy { SoundLayoutManager(this.context) }
		private val soundsManager: SoundsManager by lazy { SoundsManager(this.context, this.soundLayoutManager, this.soundSheetsDataUtil) }
		private val soundSheetsManager: SoundSheetsManager by lazy { SoundSheetsManager(this.context, this.soundLayoutManager) }

		val soundsDataAccess: SoundsDataAccess get() = this.soundsManager
		val soundsDataStorage: SoundsDataStorage get() = this.soundsManager
		val soundsDataUtil: SoundsDataUtil get() = this.soundsManager

		val soundSheetsDataAccess: SoundSheetsDataAccess get() = this.soundSheetsManager
		val soundSheetsDataStorage: SoundSheetsDataStorage get() = this.soundSheetsManager
		val soundSheetsDataUtil: SoundSheetsDataUtil get() = this.soundSheetsManager

		val soundLayoutManager: ISoundLayoutManager get() = this.mSoundLayoutsManager


		val storage by lazy { AppDataStorage(this.context) }
		val newSoundSheetManager by lazy { NewSoundSheetManager(this.context) }
		val newSoundManager by lazy { NewSoundManager() }
		val newSoundLayoutManager by lazy {
			NewSoundLayoutManager(this.context, this.storage, this.newSoundSheetManager, this.newSoundManager) }

		val randomNumber: Int get() = this.random.nextInt(Integer.MAX_VALUE)

		val taskCounter: ValueHolder<Int> by lazy { ValueHolder(0) }
	}

	override fun onCreate() {
		super.onCreate()
		staticContext = this.applicationContext

		newSoundLayoutManager.initIfRequired()
	}
}
