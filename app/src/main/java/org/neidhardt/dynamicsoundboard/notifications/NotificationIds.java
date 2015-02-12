package org.neidhardt.dynamicsoundboard.notifications;

/**
 * Created by eric.neidhardt on 15.12.2014.
 */
public class NotificationIds
{
	public static final int NOTIFICATION_ID_PLAYLIST = "org.neidhardt.dynamicsoundboard.notifications.NOTIFICATION_ID_PLAYLIST".hashCode(); // must be a random, unique id

	public static final String KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_PLAYER_ID";
	public static final String KEY_NOTIFICATION_ID = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_NOTIFICATION_ID";

	public static final String ACTION_DISMISS = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_DISMISS";
	public static final String ACTION_PLAY = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_PLAY";
	public static final String ACTION_PAUSE = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_PAUSE";
	public static final String ACTION_STOP = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_STOP";
	public static final String ACTION_FADE_OUT = "org.neidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_FADE_OUT";
}
