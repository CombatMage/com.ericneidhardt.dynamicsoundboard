package org.neidhardt.dynamicsoundboard.logger

import android.util.Log

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
object Logger {

	fun e(TAG: String, msg: String?) {
		Log.e(TAG, msg)
	}

	fun d(TAG: String, msg: String?) {
		Log.d(TAG, msg)
    }
}
