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
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * @author eric.neidhardt on 16.06.2016.
 */
class PendingSoundNotification(val notificationId: Int, var playerId: String, var notification: Notification) {

	val isPlaylistNotification: Boolean = this.notificationId == NotificationId.PLAYLIST

	companion object {
		fun getNotificationIntentFilter(): IntentFilter {
			val filter = IntentFilter()
			filter.addAction(NotificationAction.DISMISS)
			filter.addAction(NotificationAction.PLAY)
			filter.addAction(NotificationAction.STOP)
			filter.addAction(NotificationAction.FADE_OUT)
			return filter
		}

		private fun getNotification(
				player: MediaPlayerController,
				soundSheet: SoundSheet?,
				context: Context,
				isPlaylistNotification: Boolean
		): PendingSoundNotification {
			val notificationId =
					if (isPlaylistNotification)
						NotificationId.PLAYLIST
					else
						player.mediaPlayerData.playerId.hashCode()
			val builder = this.getDefaultNotification(
					context = context,
					player = player,
					soundSheet = soundSheet,
					notificationId = notificationId,
					isPlaylistNotification = isPlaylistNotification)

			return PendingSoundNotification(notificationId, player.mediaPlayerData.playerId, builder.build())
		}

		fun getNotificationForPlayer(player: MediaPlayerController, soundSheet: SoundSheet, context: Context): PendingSoundNotification
				= getNotification(player, soundSheet, context, false)

		fun getNotificationForPlaylist(player: MediaPlayerController, context: Context): PendingSoundNotification
				= getNotification(player, null, context, true)

		private fun getDefaultNotification(
				player: MediaPlayerController,
				context: Context,
				soundSheet: SoundSheet?,
				notificationId: Int,
				isPlaylistNotification: Boolean
		): NotificationCompat.Builder {
			val playerName = player.mediaPlayerData.label
			val soundSheetName = if (isPlaylistNotification) context.resources.getString(R.string.notification_playlist) else soundSheet?.label

			val playerId = player.mediaPlayerData.playerId

			val style = android.support.v4.media.app.NotificationCompat.MediaStyle().apply {
				this.setShowActionsInCompactView(0, 1)
			}

			val builder = NotificationCompat.Builder(context, ChannelId.PENDING_SOUNDS)
					.setContentTitle(playerName)
					.setContentText(soundSheetName)
					.setSmallIcon(R.drawable.ic_stat_pending_sounds)
					.setDismissPlayerIntent(
							context = context,
							notificationId = notificationId,
							playerId = player.mediaPlayerData.playerId
					)
					.setContentIntent(getOpenActivityIntent(context))
					.setLargeIcon(this.getLargeIcon(context, player))
					.setStyle(style)
					.setActionStop(context, true, notificationId, playerId)

			if (player.isPlayingSound) {
				builder.setActionFadeOut(context, true, notificationId, playerId)
			} else {
				builder.setActionPlay(context, true, notificationId, playerId)
			}

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

			return if (data != null) {
				val size = getBitmapSize(data)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				getBitmapFromBytes(data, sampleSize)
			} else {
				val size = getBitmapSize(resources, R.drawable.ic_notification_large)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				getBitmapFromAsset(context, R.drawable.ic_notification_large, sampleSize)
			}
		}

		private fun getBackgroundImageForWear(context: Context, player: MediaPlayerController): Bitmap {
			val data = player.albumCover

			val requiredHeight = 400
			val requiredWidth = 400

			return if (data != null) {
				val size = getBitmapSize(data)
				val sampleSize = getSampleFactor(size.x, size.y, requiredWidth, requiredHeight)
				getBitmapFromBytes(data, sampleSize)
			} else {
				getBitmapFromAsset(context, R.drawable.ic_notification_background_wear, 1)
			}
		}
	}

	override fun toString(): String {
		return "PendingSoundNotification(notificationId=$notificationId, playerId='$playerId', notification=$notification, isPlaylistNotification=$isPlaylistNotification)"
	}

}

private fun NotificationCompat.Builder.setActionStop(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_stop,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_stop_sound)
			else
				""
			, getPendingIntent(context, NotificationAction.STOP, notificationId, playerId))
	)
}

private fun NotificationCompat.Builder.setActionPlay(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_play,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_play_sound)
			else
				""
			, getPendingIntent(context, NotificationAction.PLAY, notificationId, playerId))
	)
}

private fun NotificationCompat.Builder.setActionFadeOut(context: Context, isLollipopStyleAvailable: Boolean, notificationId: Int, playerId: String): NotificationCompat.Builder {
	return this.addAction(buildAction(R.drawable.ic_notification_pause,
			if (isLollipopStyleAvailable)
				context.getString(R.string.notification_pause_sound)
			else
				""
			, getPendingIntent(context, NotificationAction.FADE_OUT, notificationId, playerId))
	)
}

private fun buildAction(iconId: Int, label: String, intent: PendingIntent): NotificationCompat.Action =
		NotificationCompat.Action.Builder(iconId, label, intent).build()

private fun NotificationCompat.Builder.setDismissPlayerIntent(context: Context, notificationId: Int, playerId: String): NotificationCompat.Builder =
		this.setDeleteIntent(getPendingIntent(context, NotificationAction.DISMISS, notificationId, playerId))

private fun getPendingIntent(context: Context, action: String, notificationId: Int, playerId: String): PendingIntent {
	val intent = Intent(action)
	intent.putExtra(NotificationExtra.PLAYER_ID, playerId)
	intent.putExtra(NotificationExtra.NOTIFICATION_ID, notificationId)
	return PendingIntent.getBroadcast(context, getRequestCode(notificationId, playerId), intent, 0)
}

private fun getOpenActivityIntent(context: Context): PendingIntent {
	val intent = Intent(context, SoundActivity::class.java)
	return PendingIntent.getActivity(context, IntentRequest.NOTIFICATION_OPEN_ACTIVITY, intent, 0)
}

private fun getRequestCode(notificationId: Int, playerId: String): Int {
	if (notificationId != NotificationId.PLAYLIST)
		return notificationId
	return (notificationId + playerId.hashCode()).toString().hashCode()
}