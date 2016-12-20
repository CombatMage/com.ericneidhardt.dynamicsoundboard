package org.neidhardt.dynamicsoundboard;

import org.acra.ACRA
import org.acra.annotation.ReportsCrashes
import org.greenrobot.eventbus.EventBus

@ReportsCrashes(
	mailTo = "eric.neidhardt@gmail.com"
)
class DynamicSoundboardApplication : SoundboardApplication()
{
	override fun onCreate()
	{
		super.onCreate()
		//EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()
		ACRA.init(this)
	}
}

fun SoundboardApplication.Companion.reportError(error: Throwable)
{
	ACRA.getErrorReporter().handleException(error)
}