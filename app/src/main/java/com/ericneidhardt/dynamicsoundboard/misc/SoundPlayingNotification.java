package com.ericneidhardt.dynamicsoundboard.misc;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.ericneidhardt.dynamicsoundboard.R;

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
public class SoundPlayingNotification extends NotificationCompat.Builder
{
	private static final int NOTIFICATION_ID = 0;

	public SoundPlayingNotification(Context context)
	{
		super(context);
		this.setSmallIcon(R.drawable.ic_stat_pending_sounds);
		this.setOngoing(true);
	}

	public int getId()
	{
		return NOTIFICATION_ID;
	}
}
