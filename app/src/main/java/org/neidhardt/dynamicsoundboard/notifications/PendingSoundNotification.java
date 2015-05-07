package org.neidhardt.dynamicsoundboard.notifications;

import android.app.Notification;

/**
 * Created by eric.neidhardt on 11.12.2014.
 */
public class PendingSoundNotification
{
	private int notificationId;
	public int getNotificationId()
	{
		return notificationId;
	}

	private String playerId;
	public String getPlayerId()
	{
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	private Notification notification;
	public Notification getNotification()
	{
		return this.notification;
	}
	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public PendingSoundNotification(int notificationId, String playerId, Notification notification)
	{
		this.notificationId = notificationId;
		this.playerId = playerId;
		this.notification = notification;
	}

	public boolean isPlaylistNotification()
	{
		return this.notificationId == NotificationIds.NOTIFICATION_ID_PLAYLIST;
	}
}
