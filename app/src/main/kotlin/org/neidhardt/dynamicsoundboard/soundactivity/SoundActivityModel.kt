package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import org.neidhardt.dynamicsoundboard.persistance.SaveDataIntentService


/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityModel(
		private val context: Context)
: SoundActivityContract.Model {

	override fun saveData() {
		SaveDataIntentService.writeBack(this.context)
	}
}