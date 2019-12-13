package org.neidhardt.dynamicsoundboard.notifications;

import java.util.*

/**
 * File created by eric.neidhardt on 15.12.2014.
 */
private val appUUID = UUID.randomUUID().toString()

object NotificationId {
	val PLAYLIST: Int = (appUUID + "NOTIFICATION_ID_PLAYLIST").hashCode()
}

object NotificationExtra {
	const val PLAYER_ID: String = "KEY_PLAYER_ID"
	const val NOTIFICATION_ID: String = "KEY_NOTIFICATION_ID"
}

object ChannelId {
	const val PENDING_SOUNDS = "pendingSoundsNotificationChannel"
}

object NotificationAction {
	val DISMISS: String = appUUID + "ACTION_DISMISS"
	val PLAY: String = appUUID + "ACTION_PLAY"
	val STOP: String = appUUID + "ACTION_STOP"
	val FADE_OUT: String = appUUID + "ACTION_FADE_OUT"
}