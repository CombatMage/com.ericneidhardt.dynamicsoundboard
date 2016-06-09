package org.neidhardt.dynamicsoundboard.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v7.app.NotificationCompat
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.ui_utils.utils.AndroidVersion
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * File created by eric.neidhardt on 04.12.2014.
 */
fun getNotificationIntentFilter(): IntentFilter
{
	val filter = IntentFilter()
	filter.addAction(ACTION_DISMISS)
	filter.addAction(ACTION_PLAY)
	filter.addAction(ACTION_STOP)
	filter.addAction(ACTION_FADE_OUT)
	return filter
}

data class PendingSoundNotification(val notificationId: Int, var playerId: String, var notification: Notification)
{
	fun isPlaylistNotification(): Boolean = this.notificationId == NOTIFICATION_ID_PLAYLIST
}

class PendingSoundNotificationBuilder
@JvmOverloads constructor
(
		context: Context,
		player: MediaPlayerController,
		val notificationId: Int = player.mediaPlayerData.playerId.hashCode(),
		title: String = player.mediaPlayerData.label,
		message: String? = null
) : NotificationCompat.Builder(context)
{
	val playerId: String = player.mediaPlayerData.playerId

	init
	{
		val isLollipopStyleAvailable = AndroidVersion.IS_LOLLIPOP_AVAILABLE
		this.setActionStop(context, isLollipopStyleAvailable)
		if (player.isPlayingSound)
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
		this.setScaledLargeIcon(this.getLargeIcon(context, player.mediaPlayerData.uri))

		this.addMediaStyle()
	}

	private fun getLargeIcon(context: Context, uri: String): Bitmap
	{
		val mediaDataReceiver = MediaMetadataRetriever()
		mediaDataReceiver.setDataSource(context, Uri.parse(uri))

		val resources = context.resources
		val requiredHeight = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
		val requiredWidth = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)

		val data = mediaDataReceiver.embeddedPicture
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

	private fun addMediaStyle()
	{
		val style = NotificationCompat.MediaStyle()
		style.setShowActionsInCompactView(1) // index of action play/pause
		this.setStyle(style)
	}

	private fun setActionStop(context: Context, isLollipopStyleAvailable: Boolean)
	{
		this.addAction(this.buildAction(R.drawable.ic_notification_stop,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_stop_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_STOP))
		)
	}

	private fun setActionPlay(context: Context, isLollipopStyleAvailable: Boolean)
	{
		this.addAction(this.buildAction(R.drawable.ic_notification_play,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_play_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_PLAY))
		)
	}

	private fun setActionFadeOut(context: Context, isLollipopStyleAvailable: Boolean)
	{
		this.addAction(this.buildAction( R.drawable.ic_notification_pause,
				if (isLollipopStyleAvailable)
					context.getString(R.string.notification_pause_sound)
				else
					""
				, this.getPendingIntent(context, ACTION_FADE_OUT))
		)
	}

	private fun buildAction(iconId: Int, label: String, intent: PendingIntent) : android.support.v4.app.NotificationCompat.Action
	{
		return android.support.v4.app.NotificationCompat.Action.Builder(iconId, label, intent).build()
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
		val intent = Intent(context, SoundActivity::class.java)
		return PendingIntent.getActivity(context, IntentRequest.NOTIFICATION_OPEN_ACTIVITY, intent, 0)
	}

	private fun getRequestCode(): Int
	{
		if (this.notificationId != NOTIFICATION_ID_PLAYLIST)
			return this.notificationId
		return Integer.toString(this.notificationId + this.playerId.hashCode()).hashCode()
	}
}
