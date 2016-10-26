package org.neidhardt.dynamicsoundboard.notifications

import android.support.v4.app.NotificationManagerCompat
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.searchInListForId
import org.neidhardt.dynamicsoundboard.soundmanagement.model.searchInMapForId
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import java.util.*

/**
 * @author eric.neidhardt on 15.06.2016.
 */
interface INotificationHandler {

	val pendingNotifications: Collection<PendingSoundNotification>

	fun showNotifications()

	fun dismissNotifications()

	fun dismissNotification(notificationId: Int)

	fun dismissNotificationForPlaylist()

	fun dismissNotificationForPlayer(playerId: String)

	fun onPlaylistPlayerStateChanged(player: MediaPlayerController)

	fun onGenericPlayerStateChanged(playerId: String)
}

class NotificationHandler(
		private val service: NotificationService,
		private val notificationManager: NotificationManagerCompat,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataUtil: SoundsDataUtil,
		private val soundSheetsDataAccess: SoundSheetsDataAccess
) : INotificationHandler {

	private val notifications = ArrayList<PendingSoundNotification>()
	override val pendingNotifications: Collection<PendingSoundNotification>
		get() = this.notifications

	override fun dismissNotifications() {
		this.notifications.forEach { notification -> this.notificationManager.cancel(notification.notificationId) }
		this.notifications.clear()
	}

	override fun dismissNotification(notificationId: Int) {
		this.notifications.dropLastWhile { it.notificationId == notificationId }
	}

	override fun dismissNotificationForPlaylist() {
		this.findPlaylistNotification()?.let { this.notificationManager.cancel(it.notificationId) }
	}

	override fun dismissNotificationForPlayer(playerId: String) {
		this.findNotificationForPendingPlayer(playerId)?.let { this.notificationManager.cancel(it.notificationId) }
	}

	override fun showNotifications() {
		var pendingPlaylistPlayer: MediaPlayerController? = null

		val pendingSounds = this.soundsDataAccess.currentlyPlayingSounds
		for (player in pendingSounds) {
			if (this.soundsDataUtil.isPlaylistPlayer(player.mediaPlayerData))
				pendingPlaylistPlayer = player // playlist sound is added as the last notification
			else {
				val soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(player.mediaPlayerData.fragmentTag)
						?: throw IllegalStateException("Sound sheet should not be null in this situation")
				this.addNotification(PendingSoundNotification.getNotificationForPlayer(player, soundSheet, this.service))
			}
		}

		if (pendingPlaylistPlayer != null) {
			val builder = PendingSoundNotification.getNotificationForPlaylist(pendingPlaylistPlayer, this.service)
			this.addNotification(builder)
		}
	}

	private fun addNotification(notification: PendingSoundNotification) {
		this.notifications.add(notification)
		this.notificationManager.notify(notification.notificationId, notification.notification)
	}

	override fun onPlaylistPlayerStateChanged(player: MediaPlayerController) {
		val correspondingNotification = this.findPlaylistNotification()
		if (correspondingNotification == null)
			addNotification(PendingSoundNotification.getNotificationForPlaylist(player, this.service))
		else
			updateOrRemovePendingPlaylistNotification(correspondingNotification)
	}

	override fun onGenericPlayerStateChanged(playerId: String) {
		val correspondingNotification = this.findNotificationForPendingPlayer(playerId)
		if (correspondingNotification == null) {
			searchInMapForId(playerId, soundsDataAccess.sounds)?.let { player ->
				val soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(player.mediaPlayerData.fragmentTag)
						?: throw IllegalStateException("Sound sheet should not be null in this situation")
				addNotification(PendingSoundNotification.getNotificationForPlayer(player, soundSheet, this.service))
			}
		} else
			this.updateOrRemovePendingNotification(correspondingNotification, playerId)
	}

	private fun updateOrRemovePendingNotification(notification: PendingSoundNotification, playerId: String) {
		val notificationId = notification.notificationId
		val player = searchInMapForId(playerId, soundsDataAccess.sounds)

		// if player stops playing and the service is still bound, we remove the notification
		if (player == null || !player.isPlayingSound && this.service.isActivityVisible)
			this.dismissNotificationForPlayer(playerId)
		else {
			val soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(player.mediaPlayerData.fragmentTag)
					?: throw IllegalStateException("Sound sheet should not be null in this situation")
			val updateNotification = PendingSoundNotification.getNotificationForPlayer(player, soundSheet, this.service).notification

			notification.notification = updateNotification
			notificationManager.notify(notificationId, notification.notification)
		}
	}

	private fun updateOrRemovePendingPlaylistNotification(notification: PendingSoundNotification) {
		val notificationId = notification.notificationId
		val player = this.soundsDataAccess.playlist.firstOrNull { it.isPlayingSound } ?: searchInListForId(notification.playerId, soundsDataAccess.playlist)

		if (player == null)
			this.dismissNotificationForPlaylist()
		else if (!player.isPlayingSound && this.service.isActivityVisible) // if player stops playing and the service is still bound, we remove the notification
			this.dismissNotificationForPlaylist()
		else {
			val updateNotification = PendingSoundNotification.getNotificationForPlaylist(player, this.service)

			notification.notification = updateNotification.notification
			notification.playerId = updateNotification.playerId
			notificationManager.notify(notificationId, notification.notification)
		}
	}

	private fun findNotificationForPendingPlayer(playerId: String): PendingSoundNotification?
			= this.notifications.firstOrNull { notification -> !notification.isPlaylistNotification && notification.playerId == playerId }

	private fun findPlaylistNotification(): PendingSoundNotification?
			= this.notifications.firstOrNull { notification -> notification.isPlaylistNotification }
}