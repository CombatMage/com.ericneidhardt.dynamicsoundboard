package org.neidhardt.dynamicsoundboard

import android.app.Application
import android.util.Log
import org.acra.ACRA
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.logger.ILogger


class Logger(app: Application) : ILogger {

	init {
		EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()
		ACRA.init(app)
	}

	override fun e(tag: String, msg: String?) {
		Log.e(tag, msg)
	}

	override fun d(tag: String, msg: String?) {
		Log.d(tag, msg)
	}

	override fun exception(error: Throwable) {
		ACRA.getErrorReporter().handleException(error)
	}

}