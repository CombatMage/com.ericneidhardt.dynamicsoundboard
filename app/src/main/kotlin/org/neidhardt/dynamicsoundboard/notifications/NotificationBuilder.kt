package org.neidhardt.dynamicsoundboard.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity
import org.neidhardt.utils.AndroidVersion

/**
 * @author eric.neidhardt on 16.06.2016.
 */
class PendingSoundNotification(val notificationId: Int, var playerId: String, var notification: Notification) {
	val isPlaylistNotification: Boolean = this.notificationId == NotificationConstants.NOTIFICATION_ID_PLAYLIST

	companion object {
		fun getNotificationIntentFilter(): IntentFilter {
			val filter = IntentFilter()
			filter.addAction(NotificationConstants.ACTION_DISMISS)
			filter.addAction(NotificationConstants.ACTION_PLAY)
			filter.addAction(NotificationConstants.ACTION_STOP)
			filter.addAction(NotificationConstants.ACTION_FADE_OUT)
			return filter
		}

		fun getNotificationForPlayer(player: MediaPlayerController, context: Context): PendingSoundNotification {

			val notificationId = player.mediaPlayerData.playerId.hashCode()
			val notificationTitle = player.mediaPlayerData.label

			val builder = this.getDefaultNotification(context = context, player = player, notificationId = notificationId, notificationTitle = notificationTitle)

			return PendingSoundNotification(notificationId, player.mediaPlayerData.playerId, builder.build())
		}

		fun getNotificationForPlaylist(player: MediaPlayerController, context: Context): PendingSoundNotification {

			val notificationId = NotificationConstants.NOTIFICATION_ID_PLAYLIST
			val notificationTitle = context.resources.getString(R.string.notification_playlist)
			val notificationText = player.mediaPlayerData.label

			val builder = this.getDefaultNotification(context = context, player = player, notificationId = notificationId, notificationTitle = notificationTitle)
			builder.setContentText(notificationText)

			return PendingSoundNotification(notificationId, player.mediaPlayerData.playerId, builder.build())
		}

		private fun getDefaultNotification(
				player: MediaPlayerController,
				context: Context, notificationId: Int,
				notificationTitle: String): NotificationCompat.Builder {
			val playerId = player.mediaPlayerData.playerId
			val style = android.support.v7.app.NotificationCompat.MediaStyle().apply { this.setShowActionsInCompactView(1) }// index of action play/pause
			val isLollipopStyleAvailable = AndroidVersion.IS_LOLLIPOP_AVAILABLE

			val builder = android.support.v7.app.NotificationCompat.Builder(context)
					.setContentTitle(notificationTitle)
					.setDefaultSmallIcon()
					.setDefaultDeleteIntent(
							context = context,
							notificationId = notificationId,
							playerId = player.mediaPlayerData.playerId
					)
					.setDefaultContentIntent(
							context = context
					)
					.setLargeIcon(this.getLargeIcon(context, player))
					.setStyle(style)
					.setActionStop(context, isLollipopStyleAvailable, notificationId, playerId)

			if (player.isPlayingSound)
				builder.setActionFadeOut(context, isLollipopStyleAvailable, notificationId, playerId)
			else
				builder.setActionPlay(context, isLollipopStyleAvailable, notificationId, playerId)

			val wearableExtender = NotificationCompat.WearableExtender()
					.setHintHideIcon(true)
					.setBackground(this.getBackgroundImageForWear(context, player))

			builder.extend(wearableExtender)

			return builder
		}

		private fun getLargeIcon(context: Context, player: MediaPlayerController): Bitmap {
			val data = player.albumCover

			val resources = context.resources
			val requiredHeight = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
			val requiredWidth = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)

			if (data != null) {
				val size = getBitmapSize(data)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				return getBitmapFromBytes(data, sampleSize)
			} else {
				val size = getBitmapSize(resources, R.drawable.ic_notification_large)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				return getBitmapFromAsset(context, R.drawable.ic_notification_large, sampleSize)
			}
		}

		private fun getBackgroundImageForWear(context: Context, player: MediaPlayerController): Bitmap {
			val data = player.albumCover

			val requiredHeight = 400
			val requiredWidth = 400

			if (data != null) {
				val size = getBitmapSize(data)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				return getBitmapFromBytes(data, sampleSize)
			} else {
				return getBitmapFromAsset(context, R.drawable.ic_notification_background_wear, 1)
			}
		}

	}
}

private fun NotificationCompat.Builder.setActionStop(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_stop,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_stop_sound)
			else
				""
			, getPendingIntent(context, NotificationConstants.ACTION_STOP, notificationId, playerId))
	)
}

private fun NotificationCompat.Builder.setActionPlay(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_play,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_play_sound)
			else
				""
			, getPendingIntent(context, NotificationConstants.ACTION_PLAY, notificationId, playerId))
	)
}

private fun NotificationCompat.Builder.setActionFadeOut(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_pause,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_pause_sound)
			else
				""
			, getPendingIntent(context, NotificationConstants.ACTION_FADE_OUT, notificationId, playerId))
	)
}

private fun buildAction(iconId: Int, label: String, intent: PendingIntent): NotificationCompat.Action =
		NotificationCompat.Action.Builder(iconId, label, intent).build()

private fun NotificationCompat.Builder.setDefaultSmallIcon(): NotificationCompat.Builder =
		this.setSmallIcon(R.drawable.ic_stat_pending_sounds)

private fun NotificationCompat.Builder.setDefaultDeleteIntent(context: Context, notificationId: Int, playerId: String): NotificationCompat.Builder =
		this.setDeleteIntent(getPendingIntent(context, NotificationConstants.ACTION_DISMISS, notificationId, playerId))

private fun NotificationCompat.Builder.setDefaultContentIntent(context: Context): NotificationCompat.Builder =
		this.setContentIntent(getOpenActivityIntent(context))

private fun getPendingIntent(context: Context, action: String, notificationId: Int, playerId: String): PendingIntent {
	val intent = Intent(action)
	intent.putExtra(NotificationConstants.KEY_PLAYER_ID, playerId)
	intent.putExtra(NotificationConstants.KEY_NOTIFICATION_ID, notificationId)
	return PendingIntent.getBroadcast(context, getRequestCode(notificationId, playerId), intent, 0)
}

private fun getOpenActivityIntent(context: Context): PendingIntent {
	val intent = Intent(context, SoundActivity::class.java)
	return PendingIntent.getActivity(context, IntentRequest.NOTIFICATION_OPEN_ACTIVITY, intent, 0)
}

private fun getRequestCode(notificationId: Int, playerId: String): Int {
	if (notificationId != NotificationConstants.NOTIFICATION_ID_PLAYLIST)
		return notificationId
	return Integer.toString(notificationId + playerId.hashCode()).hashCode()
}