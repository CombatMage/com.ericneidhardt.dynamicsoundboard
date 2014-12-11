package com.ericneidhardt.dynamicsoundboard.notification;

import android.app.Notification;

/**
 * Created by eric.neidhardt on 11.12.2014.
 */
public class PendingSoundNotification
{
	private int notificationId;
	private Notification notification;

	public PendingSoundNotification(int notificationId, Notification notification)
	{
		this.notificationId = notificationId;
		this.notification = notification;
	}

	public int getNotificationId()
	{
		return notificationId;
	}

	public Notification getNotification()
	{
		return this.notification;
	}
}
