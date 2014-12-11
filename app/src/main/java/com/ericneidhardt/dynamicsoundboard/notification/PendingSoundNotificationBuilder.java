package com.ericneidhardt.dynamicsoundboard.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Util;

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
public class PendingSoundNotificationBuilder extends NotificationCompat.Builder
{
	public static final String ACTION_DISMISS = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_DISMISS";

	public static final String KEY_PLAYER_ID = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_PLAYER_ID";
	public static final String KEY_NOTIFICATION_ID = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_NOTIFICATION_ID";

	private Context context;
	private int notificationId;
	private String playerId;

	public PendingSoundNotificationBuilder(Context context, EnhancedMediaPlayer player)
	{
		super(context);
		this.context = context;
		this.playerId = player.getMediaPlayerData().getPlayerId();
		this.notificationId = this.playerId.hashCode();

		this.setLargeIcon(Util.getBitmap(context, R.drawable.ic_launcher));
		this.setSmallIcon(R.drawable.ic_stat_pending_sounds);
		this.setDeleteIntent(this.getStopSoundsIntent());
		this.setContentIntent(this.getOpenActivityIntent());
		this.setContentTitle(player.getMediaPlayerData().getLabel());
	}

	public int getNotificationId()
	{
		return this.notificationId;
	}

	private PendingIntent getStopSoundsIntent()
	{
		Intent intent = new Intent(ACTION_DISMISS);
		intent.putExtra(KEY_PLAYER_ID, this.playerId);
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
