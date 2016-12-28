package org.neidhardt.dynamicsoundboard

import android.content.Context
import android.support.multidex.MultiDexApplication
import org.neidhardt.dynamicsoundboard.manager.NewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.dynamicsoundboard.manager.NewSoundSheetManager
import org.neidhardt.dynamicsoundboard.persistance.AppDataStorage
import org.neidhardt.utils.ValueHolder
import java.util.*


open class SoundboardApplication : MultiDexApplication() {

	companion object {

		private var staticContext: Context? = null
		val context: Context get() = this.staticContext as Context

		private val random = Random()

		val storage by lazy { AppDataStorage(this.context) }
		val newSoundSheetManager by lazy { NewSoundSheetManager(this.context) }
		val newSoundManager by lazy { NewSoundManager(this.context) }
		val newPlaylistManager by lazy { NewPlaylistManager(this.context) }
		val newSoundLayoutManager by lazy {
			NewSoundLayoutManager(this.context,
					this.storage,
					this.newSoundSheetManager,
					this.newPlaylistManager,
					this.newSoundManager) }

		val randomNumber: Int get() = this.random.nextInt(Integer.MAX_VALUE)

		val taskCounter: ValueHolder<Int> by lazy { ValueHolder(0) }
	}

	override fun onCreate() {
		super.onCreate()
		staticContext = this.applicationContext

		newSoundLayoutManager.initIfRequired()
	}
}
