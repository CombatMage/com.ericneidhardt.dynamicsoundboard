package org.neidhardt.dynamicsoundboard.persistance

import android.app.IntentService
import android.content.Context
import android.content.Intent
import org.neidhardt.dynamicsoundboard.SoundboardApplication

/**
 * Created by eric.neidhardt@gmail.com on 06.01.2017.
 */
class SaveDataIntentService : IntentService(SaveDataIntentService::class.java.name) {

	companion object {
		fun writeBack(context: Context) {
			val intent = Intent(context, SaveDataIntentService::class.java)
			context.startService(intent)
		}
	}

	override fun onHandleIntent(intent: Intent?) {
		val soundLayouts = SoundboardApplication.soundLayoutManager.soundLayouts
		SoundboardApplication.storage.save(soundLayouts).subscribe()
	}
}