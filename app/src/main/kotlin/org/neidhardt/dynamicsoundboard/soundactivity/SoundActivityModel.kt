package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManagerContract
import org.neidhardt.dynamicsoundboard.persistance.SaveDataIntentService
import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet


/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityModel(
		private val context: Context,
		private val soundSheetManager: SoundSheetManagerContract.Model)
: SoundActivityContract.Model {

	override fun getSoundSheets(): List<SoundSheet> {
		return this.soundSheetManager.soundSheets
	}

	override fun getNameForNewSoundSheet(): String {
		return this.soundSheetManager.suggestedName
	}

	override fun saveData() {
		SaveDataIntentService.writeBack(this.context)
	}
}