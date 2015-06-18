package org.neidhardt.dynamicsoundboard.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManagerUtil;
import org.neidhardt.dynamicsoundboard.notifications.service.NotificationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * File created by eric.neidhardt on 23.03.2015.
 */
public class NotificationHandler implements
		SharedPreferences.OnSharedPreferenceChangeListener,
		MediaPlayerEventListener
{
	private static final String TAG = NotificationHandler.class.getName();

	@Inject SoundsDataAccess soundsDataAccess;

	private NotificationService service;

	private BroadcastReceiver notificationActionReceiver;

	private NotificationManager notificationManager;
	private List<PendingSoundNotification> notifications;

	public NotificationHandler(NotificationService service, SoundsDataAccess soundsDataAccess)
	{
		this.service = service;
		this.soundsDataAccess = soundsDataAccess;

		this.notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notifications = new ArrayList<>();

		this.notificationActionReceiver = new NotificationActionReceiver();

		EventBus.getDefault().registerSticky(this);
		SoundboardPreferences.registerSharedPreferenceChangedListener(this);
		service.registerReceiver(this.notificationActionReceiver, PendingSoundNotificationBuilder.getNotificationIntentFilter());
	}

	public void onServiceDestroyed()
	{
		EventBus.getDefault().unregister(this);
		SoundboardPreferences.unregisterSharedPreferenceChangedListener(this);
		this.service.unregisterReceiver(this.notificationActionReceiver);

		this.service = null;
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

		Set<EnhancedMediaPlayer> pendingSounds = this.soundsDataAccess.getCurrentlyPlayingSounds();
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
		return new PendingSoundNotificationBuilder(this.service.getApplicationContext(), player);
	}

	private PendingSoundNotificationBuilder getNotificationForPlaylist(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(
				this.service.getApplicationContext(),
				player,
				NotificationIds.NOTIFICATION_ID_PLAYLIST,
				this.service.getString(R.string.notification_playlist),
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
		if (key.equals(service.getString(R.string.preferences_enable_notifications_key)))
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
		EnhancedMediaPlayer player = SoundsManagerUtil.searchInMapForId(playerId, soundsDataAccess.getSounds());

		if (player == null || !player.isPlaying() && this.service.isActivityVisible()) // if player stops playing and the service is still bound, we remove the notification
		{
			this.removeNotificationForPlayer(playerId);
			return true;
		}

		PendingSoundNotificationBuilder builder = new PendingSoundNotificationBuilder(this.service.getApplicationContext(), player, notificationId);

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

	public void removeNotificationsForPausedSounds()
	{
		for (PendingSoundNotification notification : this.notifications)
		{
			String playerId = notification.getPlayerId();
			boolean isInPlaylist = notification.isPlaylistNotification();

			if (isInPlaylist)
			{
				EnhancedMediaPlayer player = SoundsManagerUtil.searchInListForId(playerId, soundsDataAccess.getPlaylist());
				if (!player.isPlaying())
					this.removePlayListNotification();
			}
			else
			{
				EnhancedMediaPlayer player = SoundsManagerUtil.searchInMapForId(playerId, soundsDataAccess.getSounds());
				if (player == null || !player.isPlaying())
					this.removeNotificationForPlayer(playerId);
			}
		}
	}

	// Update notifications, according to player state or notification actions

	@Override
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
			EnhancedMediaPlayer player = SoundsManagerUtil.searchInListForId(playerId, soundsDataAccess.getPlaylist());
			if (player != null && isAlive)
				this.handlePlaylistPlayerStateChanged(player);
			else
				this.removePlayListNotification();
		}
		else // check if there is a generic notification to update
		{
			if (isAlive)
				this.handlePlayerStateChanged(playerId);
			else
				this.removeNotificationForPlayer(playerId);
		}
	}

	@Override
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		// nothing to be done
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
		EnhancedMediaPlayer player = this.getPlayingSoundFromPlaylist();
		if (player == null)
			player = SoundsManagerUtil.searchInListForId(correspondingNotification.getPlayerId(), soundsDataAccess.getPlaylist());

		if (!player.isPlaying() && this.service.isActivityVisible()) // if player stops playing and the service is still bound, we remove the notification
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
			addNotification(getNotificationForSound(SoundsManagerUtil.searchInMapForId(playerId, soundsDataAccess.getSounds())));
	}

	private EnhancedMediaPlayer getPlayingSoundFromPlaylist()
	{
		List<EnhancedMediaPlayer> playlist = soundsDataAccess.getPlaylist();
		for (EnhancedMediaPlayer player : playlist)
		{
			if (player.isPlaying())
				return player;
		}
		return null;
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
					player = SoundsManagerUtil.searchInListForId(playerId, soundsDataAccess.getPlaylist());
				else
					player = SoundsManagerUtil.searchInMapForId(playerId, soundsDataAccess.getSounds());
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

			if (!service.isActivityVisible() && notifications.size() == 0)
				service.stopSelf();
		}
	}

	// for testing
	public void setNotifications(List<PendingSoundNotification> notifications)
	{
		this.notifications = notifications;
	}
}
