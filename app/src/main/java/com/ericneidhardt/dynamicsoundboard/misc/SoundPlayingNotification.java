package com.ericneidhardt.dynamicsoundboard.misc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
public class SoundPlayingNotification extends NotificationCompat.Builder
{
	public static final int NOTIFICATION_ID = 0;

	public static final String ACTION_DISMISS = "com.ericneidhardt.dynamicsoundboard.misc.SoundPlayingNotification.ACTION_DISMISS";

	private Context context;

	public SoundPlayingNotification(Context context)
	{
		super(context);
		this.context = context;

		this.setLargeIcon(Util.getBitmap(context, R.drawable.ic_launcher));
		this.setSmallIcon(R.drawable.ic_stat_pending_sounds);
		this.setDeleteIntent(this.getStopSoundsIntent());
		this.setContentIntent(this.getOpenActivityIntent());
	}

	public void setTitle(int nrPlayingSounds)
	{
		String title = nrPlayingSounds > 1
				? nrPlayingSounds + " " + this.context.getString(R.string.notification_n_sounds_playing)
				: this.context.getString(R.string.notification_1_sounds_playing);
		this.setContentTitle(title);
	}

	private PendingIntent getStopSoundsIntent()
	{
		Intent intent = new Intent(ACTION_DISMISS);
		return PendingIntent.getBroadcast(this.context, IntentRequest.NOTIFICATION_DISMISS, intent, 0);
	}

	private PendingIntent getOpenActivityIntent()
	{
		Intent intent = new Intent(this.context, BaseActivity.class);
		return PendingIntent.getActivity(this.context, IntentRequest.NOTIFICATION_OPEN_ACTIVITY, intent, 0);
	}

	public static IntentFilter getNotificationIntentFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISMISS);
		// TODO add additional Actions
		return filter;
	}
}
