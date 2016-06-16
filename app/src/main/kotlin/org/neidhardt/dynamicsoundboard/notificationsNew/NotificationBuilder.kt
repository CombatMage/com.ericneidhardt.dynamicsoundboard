package org.neidhardt.dynamicsoundboard.notificationsNew

import android.app.Notification
import android.content.Context
import android.content.IntentFilter
import android.support.v7.app.NotificationCompat
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.notifications.NotificationConstants

/**
 * @author eric.neidhardt on 16.06.2016.
 */
class PendingSoundNotification(val notificationId: Int, var playerId: String, var notification: Notification)
{
	val isPlaylistNotification: Boolean = this.notificationId == NotificationConstants.NOTIFICATION_ID_PLAYLIST

	companion object {
		fun getNotificationIntentFilter(): IntentFilter
		{
			val filter = IntentFilter()
			filter.addAction(NotificationConstants.ACTION_DISMISS)
			filter.addAction(NotificationConstants.ACTION_PLAY)
			filter.addAction(NotificationConstants.ACTION_STOP)
			filter.addAction(NotificationConstants.ACTION_FADE_OUT)
			return filter
		}

		fun getNotificationForPlayer(player: MediaPlayerController, context: Context): PendingSoundNotification {

			val builder = NotificationCompat.Builder(context)
					.setContentTitle("TODO")
					.setContentText("TODO")
					.setSmallIcon(R.drawable.ic_action_done)

			return PendingSoundNotification(-1, player.mediaPlayerData.playerId, builder.build())
		}

		fun getNotificationForPlaylist(player: MediaPlayerController, context: Context): PendingSoundNotification {

			val builder = NotificationCompat.Builder(context)
					.setContentTitle("TODO")
					.setContentText("TODO")
					.setSmallIcon(R.drawable.ic_action_done)

			// this.service.getString(R.string.notification_playlist), player.mediaPlayerData.label
			return PendingSoundNotification(-1, player.mediaPlayerData.playerId, builder.build())
		}
	}
}
