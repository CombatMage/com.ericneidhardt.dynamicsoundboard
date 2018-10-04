package org.neidhardt.dynamicsoundboard.notifications;

import java.util.*

/**
 * File created by eric.neidhardt on 15.12.2014.
 */
object NotificationConstants {
	private val appUUID = UUID.randomUUID().toString()

	val NOTIFICATION_ID_PLAYLIST: Int = (appUUID + "NOTIFICATION_ID_PLAYLIST").hashCode()

	const val KEY_PLAYER_ID: String = "KEY_PLAYER_ID"
	const val KEY_NOTIFICATION_ID: String = "KEY_NOTIFICATION_ID"

	val ACTION_DISMISS: String = appUUID + "ACTION_DISMISS"
	val ACTION_PLAY: String = appUUID + "ACTION_PLAY"
	val ACTION_STOP: String = appUUID + "ACTION_STOP"
	val ACTION_FADE_OUT: String = appUUID + "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_FADE_OUT"
}

