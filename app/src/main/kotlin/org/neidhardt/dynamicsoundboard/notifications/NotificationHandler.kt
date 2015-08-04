package org.neidhardt.dynamicsoundboard.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.notifications.service.NotificationService
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil
import org.neidhardt.dynamicsoundboard.soundmanagement.model.searchInListForId
import org.neidhardt.dynamicsoundboard.soundmanagement.model.searchInMapForId
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 23.03.2015.
 */
public class NotificationHandler
(
		private val service: NotificationService,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataUtil: SoundsDataUtil,
		private val soundSheetsDataUtil: SoundSheetsDataUtil
) :
		SharedPreferences.OnSharedPreferenceChangeListener,
		MediaPlayerEventListener
{
	private val TAG = javaClass.getName()

	private val eventBus = EventBus.getDefault()
	private val notificationActionReceiver: BroadcastReceiver = NotificationActionReceiver()

	private val notificationManager: NotificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	private val notifications: MutableList<PendingSoundNotification> = ArrayList<PendingSoundNotification>()

	init
	{
		this.eventBus.registerSticky(this)
		SoundboardPreferences.registerSharedPreferenceChangedListener(this)
		this.service.registerReceiver(this.notificationActionReceiver, getNotificationIntentFilter())
	}

	public fun onServiceDestroyed()
	{
		this.eventBus.unregister(this)
		SoundboardPreferences.unregisterSharedPreferenceChangedListener(this)
		this.service.unregisterReceiver(this.notificationActionReceiver)
	}

	public fun dismissAllNotifications()
	{
		this.notifications.map { notification -> this.notificationManager.cancel(notification.notificationId) }
		this.notifications.clear()
	}

	private fun showAllNotifications() {
		var pendingPlaylistPlayer: EnhancedMediaPlayer? = null

		val pendingSounds = this.soundsDataAccess.getCurrentlyPlayingSounds()
		for (player in pendingSounds) {
			if (this.soundsDataUtil.isPlaylistPlayer(player.getMediaPlayerData()))
			// playlist sound is added as the last notification
				pendingPlaylistPlayer = player
			else
				this.addNotification(this.getNotificationForSound(player))
		}

		if (pendingPlaylistPlayer != null)
		{
			val builder = this.getNotificationForPlaylist(pendingPlaylistPlayer)
			this.addNotification(builder)
		}
	}

	private fun getNotificationForSound(player: EnhancedMediaPlayer): PendingSoundNotificationBuilder
			= PendingSoundNotificationBuilder(this.service.getApplicationContext(), player)

	private fun getNotificationForPlaylist(player: EnhancedMediaPlayer): PendingSoundNotificationBuilder
	{
		return PendingSoundNotificationBuilder(this.service.getApplicationContext(), player, NOTIFICATION_ID_PLAYLIST,
				this.service.getString(R.string.notification_playlist), player.getMediaPlayerData().getLabel())
	}

	private fun addNotification(notificationBuilder: PendingSoundNotificationBuilder)
	{
		val notificationId = notificationBuilder.notificationId
		val playerId = notificationBuilder.playerId

		val notification = PendingSoundNotification(notificationId, playerId, notificationBuilder.build())

		this.notifications.add(notification)
		this.notificationManager.notify(notification.notificationId, notification.notification)
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String)
	{
		if (key == service.getString(R.string.preferences_enable_notifications_key))
		{
			val areNotificationsEnabledEnabled = SoundboardPreferences.areNotificationsEnabled()
			Logger.d(TAG, "onSharedPreferenceChanged " + key + " to " + areNotificationsEnabledEnabled)

			if (areNotificationsEnabledEnabled)
				this.showAllNotifications()
			else
				this.dismissAllNotifications()
		}
	}

	private fun updateOrRemovePendingNotification(playerId: String): Boolean
	{
		val correspondingNotification = this.findNotificationForPendingPlayer(playerId) ?: return false

		val notificationId = correspondingNotification.notificationId
		val player = searchInMapForId(playerId, soundsDataAccess.getSounds())

		if (player == null || !player.isPlaying() && this.service.isActivityVisible())
		// if player stops playing and the service is still bound, we remove the notification
		{
			this.removeNotificationForPlayer(playerId)
			return true
		}

		val builder = PendingSoundNotificationBuilder(this.service.getApplicationContext(), player, notificationId)

		correspondingNotification.notification = builder.build()
		notificationManager.notify(notificationId, correspondingNotification.notification)

		return true
	}

	private fun removeNotificationForPlayer(playerId: String)
	{
		val notification = this.findNotificationForPendingPlayer(playerId)
		if (notification != null)
			notificationManager.cancel(notification.notificationId)
	}

	private fun findNotificationForPendingPlayer(playerId: String): PendingSoundNotification?
			= this.notifications.firstOrNull { notification -> !notification.isPlaylistNotification() && notification.playerId == playerId }

	private fun findPlaylistNotification(): PendingSoundNotification?
			= this.notifications.firstOrNull { notification -> notification.isPlaylistNotification() }

	public fun removeNotificationsForPausedSounds()
	{
		for (notification in this.notifications)
		{
			val playerId = notification.playerId
			val isInPlaylist = notification.isPlaylistNotification()

			if (isInPlaylist)
			{
				val player = searchInListForId(playerId, soundsDataAccess.getPlaylist())
				if (player != null && !player.isPlaying())
					this.removePlayListNotification()
			}
			else
			{
				val player = searchInMapForId(playerId, soundsDataAccess.getSounds())
				if (player == null || !player.isPlaying())
					this.removeNotificationForPlayer(playerId)
			}
		}
	}

	// Update notifications, according to player state or notification actions
	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		Logger.d(TAG, event.toString())

		val areNotificationsEnabled = SoundboardPreferences.areNotificationsEnabled()
		if (!areNotificationsEnabled)
			return

		val playerId = event.getPlayerId()
		val fragmentTag = event.getFragmentTag()
		val isAlive = event.isAlive()

		if (playerId == null || fragmentTag == null)
			return

		// update special playlist notification
		if (this.soundSheetsDataUtil.isPlaylistSoundSheet(fragmentTag))
		{
			val player = searchInListForId(playerId, soundsDataAccess.getPlaylist())
			if (player != null && isAlive)
				this.handlePlaylistPlayerStateChanged(player)
			else
				this.removePlayListNotification()
		}
		else // check if there is a generic notification to update
		{
			if (isAlive)
				this.handlePlayerStateChanged(playerId)
			else
				this.removeNotificationForPlayer(playerId)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}

	private fun handlePlaylistPlayerStateChanged(player: EnhancedMediaPlayer)
	{
		val isPendingNotification = this.updateOrRemovePendingPlaylistNotification()
		if (!isPendingNotification)
			addNotification(getNotificationForPlaylist(player))
	}

	fun removePlayListNotification()
	{
		val notification = this.findPlaylistNotification()
		if (notification != null)
			notificationManager.cancel(notification.notificationId)
	}

	private fun updateOrRemovePendingPlaylistNotification(): Boolean
	{
		val correspondingNotification = this.findPlaylistNotification() ?: return false

		val notificationId = correspondingNotification.notificationId
		var player = this.getPlayingSoundFromPlaylist() ?: searchInListForId(correspondingNotification.playerId, soundsDataAccess.getPlaylist())

		if (player != null)
		{
			// if player stops playing and the service is still bound, we remove the notification
			if (!player.isPlaying() && this.service.isActivityVisible())
			{
				this.removePlayListNotification()
				return true
			}

			val builder = getNotificationForPlaylist(player)

			correspondingNotification.playerId = player.getMediaPlayerData().getPlayerId()
			correspondingNotification.notification = builder.build()
			notificationManager.notify(notificationId, correspondingNotification.notification)
		}
		return true
	}

	private fun handlePlayerStateChanged(playerId: String)
	{
		val isPendingNotification = this.updateOrRemovePendingNotification(playerId)
		if (!isPendingNotification)
		{
			var player = searchInMapForId(playerId, soundsDataAccess.getSounds())
			if (player != null)
				addNotification(getNotificationForSound(player))
		}
	}

	private fun getPlayingSoundFromPlaylist(): EnhancedMediaPlayer? = soundsDataAccess.getPlaylist().firstOrNull { player -> player.isPlaying() }

	private inner class NotificationActionReceiver : BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent)
		{
			Logger.d(TAG, "NotificationActionReceiver.onReceive " + intent)

			val action = intent.getAction() ?: return
			val playerId = intent.getStringExtra(KEY_PLAYER_ID) ?: return

			val notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, 0)
			if (action.equals(ACTION_DISMISS))
				this.dismissPendingMediaPlayer(notificationId)
			else
			{
				val player: EnhancedMediaPlayer?
				if (notificationId == NOTIFICATION_ID_PLAYLIST)
					player = searchInListForId(playerId, soundsDataAccess.getPlaylist())
				else
					player = searchInMapForId(playerId, soundsDataAccess.getSounds())
				if (player == null)
					return

				when (action)
				{
					ACTION_PAUSE -> player.pauseSound()
					ACTION_STOP -> player.stopSound()
					ACTION_PLAY -> player.playSound()
					ACTION_FADE_OUT -> player.fadeOutSound()
					else -> {
						return
					}
				}
			}
		}

		private fun dismissPendingMediaPlayer(notificationId: Int)
		{
			notifications.dropLastWhile { notification -> notification.notificationId == notificationId }

			if (!service.isActivityVisible() && notifications.size() == 0)
				service.stopSelf()
		}
	}
}

