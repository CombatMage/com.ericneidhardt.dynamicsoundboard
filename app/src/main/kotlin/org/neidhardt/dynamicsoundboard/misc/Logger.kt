package org.neidhardt.dynamicsoundboard.misc

import android.util.Log
import org.neidhardt.dynamicsoundboard.SoundboardConfiguration

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
object Logger {

	fun e(TAG: String, msg: String?) {
		if (SoundboardConfiguration.ENABLE_LOGGING)
			Log.e(TAG, msg)
	}

	fun d(TAG: String, msg: String?) {
		if (SoundboardConfiguration.ENABLE_LOGGING)
			Log.d(TAG, msg)
    }
}
