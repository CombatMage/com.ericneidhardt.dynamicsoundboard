package org.neidhardt.dynamicsoundboard

import android.app.Application
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.logger.ILogger


class Logger(app: Application) : ILogger {

	init {
		EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()
	}

	override fun e(tag: String, msg: String?) {
	}

	override fun d(tag: String, msg: String?) {
	}

	override fun exception(error: Throwable) {
	}

}