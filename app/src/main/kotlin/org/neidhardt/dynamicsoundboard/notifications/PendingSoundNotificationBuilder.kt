package org.neidhardt.dynamicsoundboard.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.Util
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
public fun getNotificationIntentFilter(): IntentFilter
{
	val filter = IntentFilter()
	filter.addAction(ACTION_DISMISS)
	filter.addAction(ACTION_PLAY)
	filter.addAction(ACTION_STOP)
	filter.addAction(ACTION_FADE_OUT)
	return filter
}

data
public class PendingSoundNotification(public val notificationId: Int, public var playerId: String, public var notification: Notification)
{
	public fun isPlaylistNotification(): Boolean = this.notificationId == NOTIFICATION_ID_PLAYLIST
}

public class PendingSoundNotificationBuilder
jvmOverloads constructor
(
		context: Context,
		player: EnhancedMediaPlayer,
		public val notificationId: Int = player.getMediaPlayerData().getPlayerId().hashCode(),
		title: String = player.getMediaPlayerData().getLabel(),
		message: String? = null
) : Notification.Builder(context)
{
	public val playerId: String = player.getMediaPlayerData().getPlayerId()

	init
	{
		val isLollipopStyleAvailable = Util.IS_LOLLIPOP_AVAILABLE
		this.setActionStop(context, isLollipopStyleAvailable)
		if (player.isPlaying())
		{
			this.setOngoing(true)
			this.setActionFadeOut(context, isLollipopStyleAvailable)
		}
		else
		{
			this.setOngoing(false)
			this.setActionPlay(context, isLollipopStyleAvailable)
		}

		this.setSmallIcon(R.drawable.ic_stat_pending_sounds)
		this.setDeleteIntent(this.getPendingIntent(context, ACTION_DISMISS))
		this.setContentIntent(this.getOpenActivityIntent(context))

		this.setContentTitle(title)
		this.setContentText(message)
		this.setScaledLargeIcon(this.getLargeIcon(context, player.getMediaPlayerData().getUri()))

		if (isLollipopStyleAvailable)
			this.addStyleLollipop()
	}

	private fun getLargeIcon(context: Context, uri: String): Bitmap
	{
		val mediaDataReceiver = MediaMetadataRetriever()
		mediaDataReceiver.setDataSource(context, Uri.parse(uri))

		val resources = context.getResources()
		val requiredHeight = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
		val requiredWidth = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)

		val data = mediaDataReceiver.getEmbeddedPicture()
		if (data != null)
		{
			val size = getBitmapSize(data)
			val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
			return getBitmap(data, sampleSize)
		} else
		{
			val size = getBitmapSize(resources, R.drawable.ic_notification_large)
			val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
			return getBitmap(context, R.drawable.ic_notification_large, sampleSize)
		}
	}

	private fun setScaledLargeIcon(bitmap: Bitmap?)
	{
		if (bitmap == null)
			return
		this.setLargeIcon(bitmap)
	}

	TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private fun addStyleLollipop()
	{
		val style = Notification.MediaStyle()
		style.setShowActionsInCompactView(1) // index of action play/pause
		this.setStyle(style)
		this.setCategory(Notification.CATEGORY_TRANSPORT)
	}

	private fun setActionStop(context: Context, isLollipopStyleAvailable: Boolean)
	{
		val icon = Icon.createWithResource(context, R.drawable.ic_notification_stop)
		this.addAction(this.buildAction(icon,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_stop_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_STOP))
		)
	}

	private fun setActionPlay(context: Context, isLollipopStyleAvailable: Boolean)
	{
		val icon = Icon.createWithResource(context, R.drawable.ic_notification_play)
		this.addAction(this.buildAction(icon,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_play_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_PLAY))
		)
	}

	private fun setActionFadeOut(context: Context, isLollipopStyleAvailable: Boolean)
	{
		val icon = Icon.createWithResource(context, R.drawable.ic_notification_pause)
		this.addAction(this.buildAction(icon,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_pause_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_FADE_OUT))
		)
	}

	private fun buildAction(icon: Icon, label: String, intent: PendingIntent) : Notification.Action
	{

		val builder = Notification.Action.Builder(icon, label, intent);
		val action = builder.build();
		return action
	}

	private fun getPendingIntent(context: Context, action: String): PendingIntent
	{
		val intent = Intent(action)
		intent.putExtra(KEY_PLAYER_ID, this.playerId)
		intent.putExtra(KEY_NOTIFICATION_ID, this.notificationId)
		return PendingIntent.getBroadcast(context, this.getRequestCode(), intent, 0)
	}

	private fun getOpenActivityIntent(context: Context): PendingIntent
	{
		val intent = Intent(context, javaClass<SoundActivity>())
		return PendingIntent.getActivity(context, IntentRequest.NOTIFICATION_OPEN_ACTIVITY, intent, 0)
	}

	private fun getRequestCode(): Int
	{
		if (this.notificationId != NOTIFICATION_ID_PLAYLIST)
			return this.notificationId
		return Integer.toString(this.notificationId + this.playerId.hashCode()).hashCode()
	}
}
