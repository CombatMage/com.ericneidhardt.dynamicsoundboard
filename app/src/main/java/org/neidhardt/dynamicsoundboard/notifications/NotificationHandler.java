package org.neidhardt.dynamicsoundboard.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityResumedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.MusicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * File created by eric.neidhardt on 23.03.2015.
 */
public class NotificationHandler implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = NotificationHandler.class.getName();

	private MusicService musicService;

	private BroadcastReceiver notificationActionReceiver;

	private NotificationManager notificationManager;
	private List<PendingSoundNotification> notifications;

	public NotificationHandler(MusicService musicService)
	{
		this.musicService = musicService;

		this.notificationManager = (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notifications = new ArrayList<>();

		this.notificationActionReceiver = new NotificationActionReceiver();

		EventBus.getDefault().registerSticky(this);
		SoundboardPreferences.registerSharedPreferenceChangedListener(this);
		musicService.registerReceiver(this.notificationActionReceiver, PendingSoundNotificationBuilder.getNotificationIntentFilter());
	}

	public void onServiceDestroyed()
	{
		EventBus.getDefault().unregister(this);
		SoundboardPreferences.unregisterSharedPreferenceChangedListener(this);
		this.musicService.unregisterReceiver(this.notificationActionReceiver);

		this.musicService = null;
	}

	public void dismissAllNotifications()
	{
		for (PendingSoundNotification notification : this.notifications)
		{
			int notificationId = notification.getNotificationId();
			this.notificationManager.cancel(notificationId);
		}
		this.notifications.clear();
	}

	private void showAllNotifications()
	{
		EnhancedMediaPlayer pendingPlaylistPlayer = null;

		Set<EnhancedMediaPlayer> pendingSounds = this.musicService.getCurrentlyPlayingSounds();
		for (EnhancedMediaPlayer player : pendingSounds)
		{
			if (player.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG)) // playlist sound is added as the last notification
				pendingPlaylistPlayer = player;
			else
				this.addNotification(this.getNotificationForSound(player));
		}

		if (pendingPlaylistPlayer != null)
		{
			PendingSoundNotificationBuilder builder = this.getNotificationForPlaylist(pendingPlaylistPlayer);
			this.addNotification(builder);
		}
	}

	private PendingSoundNotificationBuilder getNotificationForSound(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(this.musicService.getApplicationContext(), player);
	}

	private PendingSoundNotificationBuilder getNotificationForPlaylist(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(
				this.musicService.getApplicationContext(),
				player,
				NotificationIds.NOTIFICATION_ID_PLAYLIST,
				this.musicService.getString(R.string.notification_playlist),
				player.getMediaPlayerData().getLabel());
	}

	private void addNotification(PendingSoundNotificationBuilder notificationBuilder)
	{
		int notificationId = notificationBuilder.getNotificationId();
		String playerId = notificationBuilder.getPlayerId();

		PendingSoundNotification notification = new PendingSoundNotification(notificationId, playerId, notificationBuilder.build());

		this.notifications.add(notification);
		this.notificationManager.notify(notification.getNotificationId(), notification.getNotification());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(musicService.getString(R.string.preferences_enable_notifications_key)))
		{
			boolean areNotificationsEnabledEnabled = SoundboardPreferences.areNotificationsEnabled();
			Logger.d(TAG, "onSharedPreferenceChanged " + key + " to " + areNotificationsEnabledEnabled);
			if (areNotificationsEnabledEnabled)
				this.showAllNotifications();
			else
				this.dismissAllNotifications();
		}
	}

	private boolean updateOrRemovePendingNotification(String playerId)
	{
		PendingSoundNotification correspondingNotification = this.findNotificationForPendingPlayer(playerId);
		if (correspondingNotification == null)
			return false;

		int notificationId = correspondingNotification.getNotificationId();
		EnhancedMediaPlayer player = this.musicService.searchInSoundsForId(playerId);

		if (!player.isPlaying() && this.musicService.isServiceBound()) // if player stops playing and the service is still bound, we remove the notification
		{
			this.removeNotificationForPlayer(playerId);
			return true;
		}

		PendingSoundNotificationBuilder builder = new PendingSoundNotificationBuilder(this.musicService.getApplicationContext(), player, notificationId);

		correspondingNotification.setNotification(builder.build());
		notificationManager.notify(notificationId, correspondingNotification.getNotification());

		return true;
	}

	private void removeNotificationForPlayer(String playerId)
	{
		PendingSoundNotification notification = this.findNotificationForPendingPlayer(playerId);
		if (notification != null)
			notificationManager.cancel(notification.getNotificationId());
	}

	private PendingSoundNotification findNotificationForPendingPlayer(String playerId)
	{
		for (PendingSoundNotification notification : notifications)
		{
			if (notification.isPlaylistNotification())
				continue;
			if (notification.getPlayerId().equals(playerId))
				return notification;
		}
		return null;
	}

	private PendingSoundNotification findPlaylistNotification()
	{
		for (PendingSoundNotification notification : notifications)
		{
			if (notification.isPlaylistNotification())
				return notification;
		}
		return null;
	}

	// Update notifications, according to player state or notification actions

	/**
	 * This is called by greenRobot EventBus in case the activity comes to foreground
	 * @param event delivered ActivityResumedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(ActivityResumedEvent event)
	{
		EventBus.getDefault().removeStickyEvent(event);

		if (this.musicService == null)
			return;

		for (PendingSoundNotification notification : this.notifications)
		{
			String playerId = notification.getPlayerId();
			boolean isInPlaylist = notification.isPlaylistNotification();

			if (isInPlaylist)
			{
				EnhancedMediaPlayer player = this.musicService.searchInPlaylistForId(playerId);
				if (!player.isPlaying())
					this.removePlayListNotification();
			}
			else
			{
				EnhancedMediaPlayer player = this.musicService.searchInSoundsForId(playerId);
				if (!player.isPlaying())
					this.removeNotificationForPlayer(playerId);
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		Logger.d(TAG, event.toString());

		boolean areNotificationsEnabled = SoundboardPreferences.areNotificationsEnabled();
		if (!areNotificationsEnabled)
			return;

		String playerId = event.getPlayerId();
		String fragmentTag = event.getFragmentTag();
		boolean isAlive = event.isAlive();

		if (playerId == null || fragmentTag == null)
			return;

		if (fragmentTag.equals(Playlist.TAG)) // update special playlist notification
		{
			EnhancedMediaPlayer player = this.musicService.searchInPlaylistForId(playerId);
			if (player != null)
			{
				if (isAlive)
					this.handlePlaylistPlayerStateChanged(player);
				else
					this.removePlayListNotification();
			}
		}
		else // check if there is a generic notification to update
		{
			if (isAlive)
				this.handlePlayerStateChanged(playerId);
			else
				this.removeNotificationForPlayer(playerId);
		}
	}

	private void handlePlaylistPlayerStateChanged(EnhancedMediaPlayer player)
	{
		boolean isPendingNotification = this.updateOrRemovePendingPlaylistNotification();
		if (!isPendingNotification)
			addNotification(getNotificationForPlaylist(player));
	}

	void removePlayListNotification()
	{
		PendingSoundNotification notification = this.findPlaylistNotification();
		if (notification != null)
			notificationManager.cancel(notification.getNotificationId());
	}

	private boolean updateOrRemovePendingPlaylistNotification()
	{
		PendingSoundNotification correspondingNotification = this.findPlaylistNotification();
		if (correspondingNotification == null)
			return false;

		int notificationId = correspondingNotification.getNotificationId();
		EnhancedMediaPlayer player = this.musicService.getPlayingSoundFromPlaylist();
		if (player == null)
			player = this.musicService.searchInPlaylistForId(correspondingNotification.getPlayerId());

		if (!player.isPlaying() && this.musicService.isServiceBound()) // if player stops playing and the service is still bound, we remove the notification
		{
			this.removePlayListNotification();
			return true;
		}

		PendingSoundNotificationBuilder builder = getNotificationForPlaylist(player);

		correspondingNotification.setPlayerId(player.getMediaPlayerData().getPlayerId());
		correspondingNotification.setNotification(builder.build());
		notificationManager.notify(notificationId, correspondingNotification.getNotification());

		return true;
	}

	private void handlePlayerStateChanged(String playerId)
	{
		boolean isPendingNotification = this.updateOrRemovePendingNotification(playerId);
		if (!isPendingNotification)
			addNotification(getNotificationForSound(this.musicService.searchInSoundsForId(playerId)));
	}

	private class NotificationActionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Logger.d(TAG, "NotificationActionReceiver.onReceive " + intent);

			String action = intent.getAction();
			if (action == null)
				return;

			String playerId = intent.getStringExtra(NotificationIds.KEY_PLAYER_ID);
			if (playerId == null)
				return;

			int notificationId = intent.getIntExtra(NotificationIds.KEY_NOTIFICATION_ID, 0);
			if (action.equals(NotificationIds.ACTION_DISMISS))
				this.dismissPendingMediaPlayer(notificationId);
			else
			{
				EnhancedMediaPlayer player;
				if (notificationId == NotificationIds.NOTIFICATION_ID_PLAYLIST)
					player = musicService.searchInPlaylistForId(playerId);
				else
					player = musicService.searchInSoundsForId(playerId);
				if (player == null)
					return;
				switch (action)
				{
					case NotificationIds.ACTION_PAUSE:
						player.pauseSound();
						break;
					case NotificationIds.ACTION_STOP:
						player.stopSound();
						break;
					case NotificationIds.ACTION_PLAY:
						player.playSound();
						break;
					case NotificationIds.ACTION_FADE_OUT:
						player.fadeOutSound();
						break;
				}
			}
		}

		private void dismissPendingMediaPlayer(int notificationId)
		{
			PendingSoundNotification notificationToRemove = null;
			for (PendingSoundNotification notification : notifications)
			{
				if (notification.getNotificationId() == notificationId)
					notificationToRemove = notification;
			}
			if (notificationToRemove != null)
				notifications.remove(notificationToRemove);

			if (!musicService.isServiceBound() && notifications.size() == 0)
				musicService.stopSelf();
		}
	}

	// for testing
	public void setNotifications(List<PendingSoundNotification> notifications)
	{
		this.notifications = notifications;
	}
}
