package org.neidhardt.dynamicsoundboard;

import com.squareup.leakcanary.LeakCanary
import org.acra.ACRA
import org.acra.annotation.ReportsCrashes
import org.greenrobot.eventbus.EventBus

@ReportsCrashes(
	mailTo = "eric.neidhardt@gmail.com"
)
class DynamicSoundboardApplication : SoundboardApplication() {

	init {
		EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()
	}

	override fun onCreate() {
		super.onCreate()

		/*if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return
		}
		LeakCanary.install(this)*/

		ACRA.init(this)
	}
}

fun SoundboardApplication.Companion.reportError(error: Throwable)
{
	ACRA.getErrorReporter().handleException(error)
}