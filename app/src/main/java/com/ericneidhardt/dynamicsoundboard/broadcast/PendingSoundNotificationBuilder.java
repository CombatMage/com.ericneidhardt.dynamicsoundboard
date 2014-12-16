package com.ericneidhardt.dynamicsoundboard.broadcast;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Util;

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
public class PendingSoundNotificationBuilder extends Notification.Builder
{
	private Context context;
	private Resources resources;

	private String playerId;
	public String getPlayerId()
	{
		return this.playerId;
	}

	private int notificationId;
	public int getNotificationId()
	{
		return this.notificationId;
	}



	public PendingSoundNotificationBuilder(Context context, EnhancedMediaPlayer player)
	{
		this(context, player, player.getMediaPlayerData().getPlayerId().hashCode());
	}

	public PendingSoundNotificationBuilder(Context context, EnhancedMediaPlayer player, int notificationId)
	{
		this(context, player, notificationId, player.getMediaPlayerData().getLabel(), null);
	}

	public PendingSoundNotificationBuilder(Context context, EnhancedMediaPlayer player, int notificationId, String title, String message)
	{
		super(context);
		this.context = context;
		this.resources = this.context.getResources();
		this.playerId = player.getMediaPlayerData().getPlayerId();
		this.notificationId = notificationId;

		this.setActionStop();
		if (player.isPlaying())
			this.setActionPause();
		else
			this.setActionPlay();

		this.setSmallIcon(R.drawable.ic_stat_pending_sounds);
		this.setDeleteIntent(this.getPendingIntent(Constants.ACTION_DISMISS));
		this.setContentIntent(this.getOpenActivityIntent());

		this.setContentTitle(title);
		this.setContentText(message);
		this.setLargeIcon(Util.getBitmap(context, R.drawable.ic_launcher));

		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP)
			this.addStyleLollipop();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void addStyleLollipop()
	{
		Notification.MediaStyle style = new Notification.MediaStyle();
		style.setShowActionsInCompactView(1); // index of action play / pause
		this.setStyle(style);
	}

	private void setActionStop()
	{
		this.addAction(R.drawable.ic_stop, this.resources.getString(R.string.notification_stop_sound), this.getPendingIntent(Constants.ACTION_STOP));
	}

	private void setActionPause()
	{
		this.addAction(R.drawable.ic_pause, this.resources.getString(R.string.notification_pause_sound), this.getPendingIntent(Constants.ACTION_PAUSE));
	}

	private void setActionPlay()
	{
		this.addAction(R.drawable.ic_play, this.resources.getString(R.string.notification_play_sound), this.getPendingIntent(Constants.ACTION_PLAY));
	}

	private PendingIntent getPendingIntent(String action)
	{
		Intent intent = new Intent(action);
		intent.putExtra(Constants.KEY_PLAYER_ID, this.playerId);
		intent.putExtra(Constants.KEY_NOTIFICATION_ID, this.notificationId);
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
		filter.addAction(Constants.ACTION_DISMISS);
		filter.addAction(Constants.ACTION_PLAY);
		filter.addAction(Constants.ACTION_PAUSE);
		filter.addAction(Constants.ACTION_STOP);
		return filter;
	}
}
