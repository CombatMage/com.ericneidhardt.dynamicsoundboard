package org.neidhardt.dynamicsoundboard.notifications

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.support.v4.app.NotificationManagerCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.findById
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEventListener
import org.neidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener
import org.neidhardt.dynamicsoundboard.soundcontrol.registerPauseSoundOnCallListener
import org.neidhardt.dynamicsoundboard.soundcontrol.unregisterPauseSoundOnCallListener
import org.neidhardt.eventbus_utils.registerIfRequired

/**
 * @author eric.neidhardt on 15.06.2016.
 */
class NotificationService : Service(),
		ActivityStateChangedEventListener,
		SharedPreferences.OnSharedPreferenceChangeListener,
		MediaPlayerEventListener
{
	companion object {

		fun start(context: Context) {
			context.startService(Intent(context, NotificationService::class.java))
		}
	}

	private val TAG: String = javaClass.name

	private val soundManager = SoundboardApplication.newSoundManager
	private val playlistManager = SoundboardApplication.newPlaylistManager
	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private val soundLayoutManager = SoundboardApplication.newSoundLayoutManager

	private val eventBus = EventBus.getDefault()
	private val phoneStateListener: PauseSoundOnCallListener = PauseSoundOnCallListener()
	private val notificationActionReceiver: BroadcastReceiver = NotificationActionReceiver({ action, playerId, notificationId ->
		this.onNotificationAction(action, playerId, notificationId)
	})

	private var notificationHandler: INotificationHandler? = null

	var isActivityVisible: Boolean = false
		private set

	override fun onBind(intent: Intent): IBinder? = null

	override fun onCreate() {
		super.onCreate()
		this.notificationHandler = NotificationHandler(
				service = this,
				notificationManager = NotificationManagerCompat.from(this),
				soundsManager = this.soundManager,
				playlistManager = this.playlistManager,
				soundSheetManager = this.soundSheetManager,
				soundLayoutManager = this.soundLayoutManager)

		this.eventBus.registerIfRequired(this)
		SoundboardPreferences.registerSharedPreferenceChangedListener(this)
		this.registerReceiver(this.notificationActionReceiver, PendingSoundNotification.getNotificationIntentFilter())
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Logger.d(TAG, "onStartCommand")
		this.isActivityVisible = true
		return START_STICKY
	}

	override fun onDestroy() {
		Logger.d(TAG, "onDestroy")

		this.unregisterPauseSoundOnCallListener(this.phoneStateListener)
		SoundboardPreferences.unregisterSharedPreferenceChangedListener(this)
		this.unregisterReceiver(this.notificationActionReceiver)
		this.eventBus.unregister(this)

		super.onDestroy()
	}

	private fun stopIfNotRequired() {
		if (!this.isActivityVisible && this.notificationHandler?.pendingNotifications?.size == 0)
			this.stopSelf()
	}

	private fun onNotificationAction(action: String, playerId: String, notificationId: Int) {
		val player: MediaPlayerController? =
				if (notificationId == NotificationConstants.NOTIFICATION_ID_PLAYLIST)
					this.playlistManager.playlist.findById(playerId)
				else
					this.soundManager.sounds.findById(playerId)

		player?.let {
			when (action) {
				NotificationConstants.ACTION_DISMISS -> {
					if (!this.isActivityVisible) player.destroy(false)
					else {}
				}
				NotificationConstants.ACTION_STOP -> player.stopSound()
				NotificationConstants.ACTION_PLAY -> player.playSound()
				NotificationConstants.ACTION_FADE_OUT -> player.fadeOutSound()
				else -> Logger.e(TAG, "unknown notification action received")
			}
		}

		this.stopIfNotRequired()
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
		if (key == this.getString(R.string.preferences_enable_notifications_key)) {
			val areNotificationsEnabledEnabled = SoundboardPreferences.areNotificationsEnabled()
			Logger.d(TAG, "onSharedPreferenceChanged $key to $areNotificationsEnabledEnabled")

			if (areNotificationsEnabledEnabled)
				this.notificationHandler?.showNotifications()
			else
				this.notificationHandler?.dismissNotifications()

			this.stopIfNotRequired()
		}
	}

	@Subscribe(sticky = true)
	override fun onEvent(event: ActivityStateChangedEvent) {
		if (event.isActivityClosed) {
			this.isActivityVisible = false

			this.registerPauseSoundOnCallListener(this.phoneStateListener)
			if (this.soundLayoutManager.currentlyPlayingSounds.isEmpty())
				this.stopSelf()
		}
		else if (event.isActivityResumed) {
			this.unregisterPauseSoundOnCallListener(this.phoneStateListener)

			this.isActivityVisible = true
			this.removeNotificationForPausedSounds()
		}
	}

	private fun removeNotificationForPausedSounds() {
		this.notificationHandler?.let { notificationHandler ->

			for (notification in notificationHandler.pendingNotifications) {
				val playerId = notification.playerId
				val isInPlaylist = notification.isPlaylistNotification

				if (isInPlaylist) {
					val player = this.playlistManager.playlist.findById(playerId)
					if (player != null && !player.isPlayingSound)
						notificationHandler.dismissNotificationForPlaylist()
				}
				else {
					val player = this.soundManager.sounds.findById(playerId)
					if (player == null || !player.isPlayingSound)
						notificationHandler.dismissNotificationForPlayer(playerId)
				}
			}
		}

		this.stopIfNotRequired()
	}

	@Subscribe(sticky = true)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		Logger.d(TAG, event.toString())

		val areNotificationsEnabled = SoundboardPreferences.areNotificationsEnabled()
		if (!areNotificationsEnabled)
			return

		val playerId = event.playerId
		val fragmentTag = event.fragmentTag
		val isAlive = event.isAlive

		// update special playlist notification
		if (fragmentTag == PlaylistTAG)
			this.handlePlaylistPlayerStateChanged(playerId, isAlive)
		else // check if there is a generic notification to update
			this.handlerPlayerStateChanged(playerId, isAlive)
	}

	private fun handlePlaylistPlayerStateChanged(playerId: String, isAlive: Boolean) {
		val player = this.playlistManager.playlist.findById(playerId)
		if (player != null && isAlive)
			this.notificationHandler?.onPlaylistPlayerStateChanged(player)
		else
			this.notificationHandler?.dismissNotificationForPlaylist()

		this.stopIfNotRequired()
	}

	private fun handlerPlayerStateChanged(playerId: String, isAlive: Boolean) {
		if (isAlive)
			this.notificationHandler?.onGenericPlayerStateChanged(playerId)
		else
			this.notificationHandler?.dismissNotificationForPlayer(playerId)

		this.stopIfNotRequired()
	}

	// unused
	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}