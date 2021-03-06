package org.neidhardt.dynamicsoundboard.notifications

import android.annotation.TargetApi
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.neidhardt.androidutils.AndroidVersion
import org.neidhardt.dynamicsoundboard.R


/**
 * Created by neid_ei (eric.neidhardt@dlr.de)
 * on 15.02.2018.
 */
object NotificationChannelBuilder {

	@TargetApi(Build.VERSION_CODES.O)
	fun createNotificationChannelForPendingSounds(context: Context) {
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val channelId = ChannelId.PENDING_SOUNDS
		val channelName = context.getString(R.string.notificationchannel_name)
		val importance = NotificationManager.IMPORTANCE_LOW

		if (AndroidVersion.IS_OREO_AVAILABLE) {
			val notificationChannel = NotificationChannel(channelId, channelName, importance)
			notificationChannel.enableLights(true)
			notificationManager.createNotificationChannel(notificationChannel)
		}
	}

}