package com.ericneidhardt.dynamicsoundboard.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;

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
		this.setScaledLargeIcon(this.getLargeIcon(player.getMediaPlayerData().getUri()));

		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP)
			this.addStyleLollipop();
	}

	private Bitmap getLargeIcon(String uri)
	{
		MediaMetadataRetriever mediaDataReceiver = new MediaMetadataRetriever();
		mediaDataReceiver.setDataSource(this.context, Uri.parse(uri));

		int requiredHeight = (int) this.resources.getDimension(android.R.dimen.notification_large_icon_height);
		int requiredWidth = (int) this.resources.getDimension(android.R.dimen.notification_large_icon_width);

		byte [] data = mediaDataReceiver.getEmbeddedPicture();
		if (data != null)
		{
			Point size = BitmapUtil.getBitmapSize(data);
			int sampleSize = BitmapUtil.getSampleFactor(size.x, size.y, requiredWidth, requiredHeight);
			return BitmapUtil.getBitmap(data, sampleSize);
		}
		else
		{
			return BitmapUtil.getBitmap(this.context, R.raw.ic_notification_large, 1);
		}
	}

	private void setScaledLargeIcon(Bitmap bitmap)
	{
		if (bitmap == null)
			return;
		this.setLargeIcon(bitmap);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void addStyleLollipop()
	{
		Notification.MediaStyle style = new Notification.MediaStyle();
		style.setShowActionsInCompactView(1); // index of action play/pause
		this.setStyle(style);
		this.setCategory(Notification.CATEGORY_TRANSPORT);
	}

	private void setActionStop()
	{
		this.addAction(R.drawable.ic_notification_stop, this.resources.getString(R.string.notification_stop_sound), this.getPendingIntent(Constants.ACTION_STOP));
	}

	private void setActionPause()
	{
		this.addAction(R.drawable.ic_notification_pause, this.resources.getString(R.string.notification_pause_sound), this.getPendingIntent(Constants.ACTION_PAUSE));
	}

	private void setActionPlay()
	{
		this.addAction(R.drawable.ic_notification_play, this.resources.getString(R.string.notification_play_sound), this.getPendingIntent(Constants.ACTION_PLAY));
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
