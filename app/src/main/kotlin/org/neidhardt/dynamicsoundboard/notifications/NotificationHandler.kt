package org.neidhardt.dynamicsoundboard.notifications

import android.support.v4.app.NotificationManagerCompat
import org.neidhardt.dynamicsoundboard.manager.*
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.logger.Logger
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
		private val playlistManager: PlaylistManager,
		private val soundsManager: SoundManager,
		private val soundSheetManager: SoundSheetManager,
		private val soundLayoutManager: SoundLayoutManager
) : INotificationHandler {

	private val logTag: String = javaClass.name

	override val pendingNotifications = ArrayList<PendingSoundNotification>()

	override fun dismissNotifications() {
		this.pendingNotifications.forEach { notification -> this.notificationManager.cancel(notification.notificationId) }
		this.pendingNotifications.clear()
	}

	override fun dismissNotification(notificationId: Int) {
		Logger.d(logTag, "dismissNotification: $notificationId")
		this.pendingNotifications.removeIf { it.notificationId == notificationId }
	}

	override fun dismissNotificationForPlaylist() {
		Logger.d(logTag, "dismissNotificationForPlaylist")
		this.findPlaylistNotification()?.let { this.notificationManager.cancel(it.notificationId) }
	}

	override fun dismissNotificationForPlayer(playerId: String) {
		Logger.d(logTag, "dismissNotificationForPlayer: $playerId")
		this.findNotificationForPendingPlayer(playerId)?.let { this.notificationManager.cancel(it.notificationId) }
	}

	override fun showNotifications() {
		var pendingPlaylistPlayer: MediaPlayerController? = null

		val pendingSounds = this.soundLayoutManager.currentlyPlayingSounds
		for (player in pendingSounds) {
			if (this.playlistManager.playlist.contains(player))
				pendingPlaylistPlayer = player // playlist sound is added as the last notification
			else {
				val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(player.mediaPlayerData.fragmentTag)
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
		this.pendingNotifications.add(notification)
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
			this.soundsManager.sounds.findById(playerId)?.let { player ->
				val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(player.mediaPlayerData.fragmentTag)
						?: throw IllegalStateException("Sound sheet should not be null in this situation")
				addNotification(PendingSoundNotification.getNotificationForPlayer(player, soundSheet, this.service))
			}
		} else
			this.updateOrRemovePendingNotification(correspondingNotification, playerId)
	}

	private fun updateOrRemovePendingNotification(notification: PendingSoundNotification, playerId: String) {
		val notificationId = notification.notificationId
		val player = this.soundsManager.sounds.findById(playerId)

		// if player stops playing and the service is still bound, we remove the notification
		if (player == null || !player.isPlayingSound && this.service.isActivityVisible)
			this.dismissNotificationForPlayer(playerId)
		else {
			val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(player.mediaPlayerData.fragmentTag)
					?: throw IllegalStateException("Sound sheet should not be null in this situation")
			val updateNotification = PendingSoundNotification.getNotificationForPlayer(player, soundSheet, this.service).notification

			notification.notification = updateNotification
			notificationManager.notify(notificationId, notification.notification)
		}
	}

	private fun updateOrRemovePendingPlaylistNotification(notification: PendingSoundNotification) {
		val notificationId = notification.notificationId
		val playerId = notification.playerId
		val player = this.playlistManager.playlist.firstOrNull { it.isPlayingSound } ?: this.playlistManager.playlist.findById(playerId)

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
			= this.pendingNotifications.firstOrNull { notification -> !notification.isPlaylistNotification && notification.playerId == playerId }

	private fun findPlaylistNotification(): PendingSoundNotification?
			= this.pendingNotifications.firstOrNull { notification -> notification.isPlaylistNotification }
}