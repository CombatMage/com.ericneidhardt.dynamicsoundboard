package org.neidhardt.dynamicsoundboard.longtermtask

import android.os.Handler
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.Logger
import roboguice.util.SafeAsyncTask

/**
 * File created by eric.neidhardt on 24.03.2015.
 */

abstract class LongTermTask<T> : SafeAsyncTask<T>()
{
	protected abstract val TAG: String

	private val eventBus = EventBus.getDefault()

	@Throws(Exception::class)
	override fun onPreExecute() {
		super.onPreExecute()
		SoundboardApplication.taskCounter += 1
	}

	@Throws(Exception::class)
	override fun onSuccess(result: T) {
		super.onSuccess(result)
		SoundboardApplication.taskCounter -= 1
	}

	@Throws(RuntimeException::class)
	override fun onException(e: Exception) {
		super.onException(e)
		Logger.e(TAG, e.message)
		SoundboardApplication.taskCounter -= 1
		throw RuntimeException(e)
	}
}
