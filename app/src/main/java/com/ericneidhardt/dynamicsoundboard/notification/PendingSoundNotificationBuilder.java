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
	public static final String ACTION_PLAY = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_PLAY";
	public static final String ACTION_PAUSE = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_PAUSE";
	public static final String ACTION_STOP = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.ACTION_STOP";

	public static final String KEY_PLAYER_ID = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_PLAYER_ID";
	public static final String KEY_NOTIFICATION_ID = "com.ericneidhardt.dynamicsoundboard.notification.SoundPlayingNotification.KEY_NOTIFICATION_ID";

	private Context context;
	private String playerId;

	private int notificationId;
	public int getNotificationId()
	{
		return this.notificationId;
	}

	public PendingSoundNotificationBuilder(Context context, EnhancedMediaPlayer player)
	{
		super(context);
		this.context = context;
		this.playerId = player.getMediaPlayerData().getPlayerId();
		this.notificationId = this.playerId.hashCode();

		this.setLargeIcon(Util.getBitmap(context, R.drawable.ic_launcher));
		this.setSmallIcon(R.drawable.ic_stat_pending_sounds);
		this.setDeleteIntent(this.getPendingIntent(ACTION_DISMISS));
		this.setContentIntent(this.getOpenActivityIntent());
		this.setContentTitle(player.getMediaPlayerData().getLabel());

		this.setActionStop();
		if (player.isPlaying())
			this.setActionPause();
		else
			this.setActionPlay();
	}

	private void setActionStop()
	{
		this.addAction(R.drawable.ic_stop, "", this.getPendingIntent(ACTION_STOP));
	}

	private void setActionPause()
	{
		this.addAction(R.drawable.ic_pause, "", this.getPendingIntent(ACTION_PAUSE));
	}

	private void setActionPlay()
	{
		this.addAction(R.drawable.ic_play, "", this.getPendingIntent(ACTION_PLAY));
	}

	private PendingIntent getPendingIntent(String action)
	{
		Intent intent = new Intent(action);
		intent.putExtra(KEY_PLAYER_ID, this.playerId);
		intent.putExtra(KEY_NOTIFICATION_ID, this.notificationId);
		return PendingIntent.getBroadcast(this.context, this.notificationId, intent, 0);
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
		filter.addAction(ACTION_PLAY);
		filter.addAction(ACTION_PAUSE);
		filter.addAction(ACTION_STOP);
		return filter;
	}
}
