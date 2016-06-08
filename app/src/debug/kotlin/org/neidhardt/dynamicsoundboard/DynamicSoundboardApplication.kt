package org.neidhardt.dynamicsoundboard;

import org.acra.ACRA
import org.acra.annotation.ReportsCrashes


@ReportsCrashes(
	mailTo = "eric@neidhardt-erkner.de"
)
class DynamicSoundboardApplication : SoundboardApplication()
{
	override fun onCreate()
	{
		super.onCreate()
		EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()
		ACRA.init(this)
	}
}

fun SoundboardApplication.Companion.reportError(error: Exception)
{
	ACRA.getErrorReporter().handleException(error)
}