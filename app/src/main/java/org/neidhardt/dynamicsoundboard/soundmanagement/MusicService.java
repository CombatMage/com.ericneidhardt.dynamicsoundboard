package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import org.neidhardt.dynamicsoundboard.notifications.NotificationIds;
import org.neidhardt.dynamicsoundboard.notifications.PendingSoundNotification;
import org.neidhardt.dynamicsoundboard.notifications.PendingSoundNotificationBuilder;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = MusicService.class.getName();

	public static final String ACTION_FINISHED_LOADING_PLAYLIST = "org.neidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_PLAYLIST";
	public static final String ACTION_FINISHED_LOADING_SOUNDS = "org.neidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_SOUNDS";

	private static final String DB_SOUNDS = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private DaoSession dbPlaylist;
	private synchronized MediaPlayerDataDao getPlaylistDao()
	{
		return this.dbPlaylist.getMediaPlayerDataDao();
	}
	private List<EnhancedMediaPlayer> playlist = new ArrayList<>();
	List<EnhancedMediaPlayer> getPlaylist()
	{
		return playlist;
	}

	private DaoSession dbSounds;
	private synchronized MediaPlayerDataDao getSoundsDao()
	{
		return this.dbSounds.getMediaPlayerDataDao();
	}
	private Map<String, List<EnhancedMediaPlayer>> sounds = new HashMap<>();
	Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return sounds;
	}

	private LocalBroadcastManager broadcastManager;
	private BroadcastReceiver notificationActionReceiver;
	private Binder binder;

	private NotificationManager notificationManager;
	private List<PendingSoundNotification> notifications;

	private boolean isServiceBound = false;

	@Override
	public IBinder onBind(Intent intent)
	{
		this.isServiceBound = true;
		return this.binder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		this.isServiceBound = false;
		return true; // this is necessary to ensure onRebind is called
	}

	@Override
	public void onRebind(Intent intent) {
		this.isServiceBound = true;
	}

	@Override
	public void onCreate()
	{
		Logger.d(TAG, "onCreate");

		super.onCreate();

		this.binder = new Binder();
		this.broadcastManager = LocalBroadcastManager.getInstance(this);
		this.notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notifications = new ArrayList<>();

		this.notificationActionReceiver = new NotificationActionReceiver();

		EventBus.getDefault().register(this);
		SoundboardPreferences.registerSharedPreferenceChangedListener(this);
		this.registerReceiver(this.notificationActionReceiver, PendingSoundNotificationBuilder.getNotificationIntentFilter());

		this.dbPlaylist = Util.setupDatabase(this.getApplicationContext(), DB_SOUNDS_PLAYLIST);
		this.dbSounds = Util.setupDatabase(this.getApplicationContext(), DB_SOUNDS);

		SafeAsyncTask task = new LoadSoundsTask();
		task.execute();

		task = new LoadPlaylistTask();
		task.execute();
	}

	@Override
	public void onDestroy()
	{
		Logger.d(TAG, "onDestroy");

		EventBus.getDefault().unregister(this);
		SoundboardPreferences.unregisterSharedPreferenceChangedListener(this);
		this.unregisterReceiver(this.notificationActionReceiver);

		this.storeLoadedSounds();
		this.dismissAllNotifications();
		this.releaseMediaPlayers();

		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(this.getString(R.string.preferences_enable_notifications_key)))
		{
			boolean areNotificationsEnabledEnabled = SoundboardPreferences.areNotificationsEnabled();
			Logger.d(TAG, "onSharedPreferenceChanged " + key + " to " + areNotificationsEnabledEnabled);
			if (areNotificationsEnabledEnabled)
				this.showAllNotifications();
			else
				this.dismissAllNotifications();
		}
	}

	private void showAllNotifications()
	{
		List<EnhancedMediaPlayer> pendingSounds = this.getPlayingSoundsFromSoundList();
		for (EnhancedMediaPlayer player : pendingSounds)
		{
			this.addNotification(this.getNotificationForSound(player));
		}

		EnhancedMediaPlayer player = this.getPlayingSoundFromPlaylist();
		if (player != null)
		{
			PendingSoundNotificationBuilder builder = this.getNotificationForPlaylist(player);
			this.addNotification(builder);
		}
	}

	private void dismissAllNotifications()
	{
		for (PendingSoundNotification notification : this.notifications)
		{
			int notificationId = notification.getNotificationId();
			this.notificationManager.cancel(notificationId);
		}
		this.notifications.clear();
	}

	private void releaseMediaPlayers()
	{
		for (EnhancedMediaPlayer player : this.playlist)
			player.destroy(false);
		Collection<List<EnhancedMediaPlayer>> allPlayers = this.sounds.values();
		for (List<EnhancedMediaPlayer> players : allPlayers)
		{
			for (EnhancedMediaPlayer player : players)
				player.destroy(false);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Logger.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	public void onActivityClosed()
	{
		Logger.d(TAG, "onActivityClosed");
		List<EnhancedMediaPlayer> pendingPlayers = this.getCurrentlyPlayingSounds();
		if (pendingPlayers.size() == 0)
			this.stopSelf();
	}

	private PendingSoundNotificationBuilder getNotificationForSound(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(this.getApplicationContext(), player);
	}

	private PendingSoundNotificationBuilder getNotificationForPlaylist(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(
				this.getApplicationContext(),
				player,
				NotificationIds.NOTIFICATION_ID_PLAYLIST,
				this.getString(R.string.notification_playlist),
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

	public void storeLoadedSounds()
	{
		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, dbSounds);
		task.execute();

		task = new UpdateSoundsTask(this.playlist, dbPlaylist);
		task.execute();
	}

	public List<EnhancedMediaPlayer> getCurrentlyPlayingSounds()
	{
		List<EnhancedMediaPlayer> currentlyPlayingSounds = this.getPlayingSoundsFromSoundList();
		EnhancedMediaPlayer soundFromPlaylist = this.getPlayingSoundFromPlaylist();
		if (soundFromPlaylist != null)
			currentlyPlayingSounds.add(soundFromPlaylist);
		return currentlyPlayingSounds;
	}

	private List<EnhancedMediaPlayer> getPlayingSoundsFromSoundList()
	{
		List<EnhancedMediaPlayer> currentlyPlayingSounds = new ArrayList<>();
		for (String fragmentTag : this.sounds.keySet())
		{
			for (EnhancedMediaPlayer player : this.sounds.get(fragmentTag))
			{
				if (player.isPlaying())
					currentlyPlayingSounds.add(player);
			}
		}
		return currentlyPlayingSounds;
	}

	private EnhancedMediaPlayer getPlayingSoundFromPlaylist()
	{
		for (EnhancedMediaPlayer sound : this.playlist)
		{
			if (sound.isPlaying())
				return sound;
		}
		return null;
	}

	public void addNewSoundToServiceAndDatabase(MediaPlayerData playerData)
	{
		String fragmentTag = playerData.getFragmentTag();
		List<EnhancedMediaPlayer> soundInFragment = this.sounds.get(fragmentTag);
		int sortOrder = soundInFragment == null ? 0 : soundInFragment.size();
		playerData.setSortOrder(sortOrder);

		EnhancedMediaPlayer player = this.createSoundFromRawData(playerData);
		if (player == null)
			return;
		this.addSoundToSounds(player);

		if (player.getMediaPlayerData() != null)
		{
			MediaPlayerDataDao soundsDao = this.getSoundsDao();
			soundsDao.insert(player.getMediaPlayerData());
		}
	}

	/**
	 * Adds sound to corresponding sound list. If the list is long enough, the players sortorder is respected, otherwise it is added to the end of the list
	 * @param player the new player to add
	 */
	private void addSoundToSounds(EnhancedMediaPlayer player)
	{
		if (player == null)
			throw new NullPointerException("cannot add new Player, player is null");
		String fragmentTag = player.getMediaPlayerData().getFragmentTag();
		int index = player.getMediaPlayerData().getSortOrder();
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);
		int count = soundsInFragment.size();
		if (index <= count) // add item according to sortorder
			soundsInFragment.add(index, player);
		else
			soundsInFragment.add(player); // if the list is to short, just append
	}

	/**
	 * Creates an new EnhancedMediaPlayer instance
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private EnhancedMediaPlayer createSoundFromRawData(MediaPlayerData playerData)
	{
		try
		{
			return new EnhancedMediaPlayer(playerData);
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			this.removeSoundFromDatabase(this.getSoundsDao(), playerData);
			return null;
		}
	}

	public void addNewSoundToPlaylist(MediaPlayerData playerData)
	{
		MediaPlayerData dataToStore = this.createPlaylistSoundFromPlayerData(playerData);
		if (dataToStore != null)
		{
			MediaPlayerDataDao playlistDao = this.getPlaylistDao();
			playlistDao.insert(dataToStore); // it is important to use data returned from createPlaylistSoundFromPlayerData, because it is a new instance
		}
	}

	/**
	 * Creates an new EnhancedMediaPlayer instance and adds this instance to the playlist.
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private MediaPlayerData createPlaylistSoundFromPlayerData(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = EnhancedMediaPlayer.getInstanceForPlayList(playerData);
			this.playlist.add(player);
			return player.getMediaPlayerData();
		} catch (IOException e)
		{
			Logger.d(TAG, playerData.toString()+ " " + e.getMessage());
			this.removeSoundFromDatabase(this.getPlaylistDao(), playerData);
			return null;
		}
	}

	public void removeSounds(String fragmentTag)
	{
		this.removeSounds(this.sounds.get(fragmentTag));
	}

	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		if (soundsToRemove == null || soundsToRemove.size() == 0)
			return;

		List<EnhancedMediaPlayer> copyList = new ArrayList<>(soundsToRemove.size());
		copyList.addAll(soundsToRemove); // this is done to prevent concurrent modification exception

		for (EnhancedMediaPlayer playerToRemove : copyList)
		{
			MediaPlayerData data = playerToRemove.getMediaPlayerData();
			this.sounds.get(data.getFragmentTag()).remove(playerToRemove);

			if (data.getIsInPlaylist())
			{
				EnhancedMediaPlayer correspondingPlayerInPlaylist = this.searchInPlaylistForId(data.getPlayerId());
				this.playlist.remove(correspondingPlayerInPlaylist);

				this.destroyPlayerAndUpdateDatabase(this.getPlaylistDao(), correspondingPlayerInPlaylist);
			}
			this.destroyPlayerAndUpdateDatabase(this.getSoundsDao(), playerToRemove);
		}
	}

	public void removeFromPlaylist(List<EnhancedMediaPlayer> playersToRemove)
	{
		for (EnhancedMediaPlayer player : playersToRemove)
			this.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), false);
	}

	public void toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		try
		{
			EnhancedMediaPlayer player = this.searchInSoundsForId(playerId);
			EnhancedMediaPlayer playerInPlaylist = this.searchInPlaylistForId(playerId);

			if (addToPlayList)
			{
				if (playerInPlaylist != null)
					return;

				player.setIsInPlaylist(true);
				playerInPlaylist = EnhancedMediaPlayer.getInstanceForPlayList(player.getMediaPlayerData());
				this.playlist.add(playerInPlaylist);
			}
			else
			{
				if (playerInPlaylist == null)
					return;

				if (player != null)
					player.setIsInPlaylist(false);

				this.playlist.remove(playerInPlaylist);
				this.destroyPlayerAndUpdateDatabase(this.getPlaylistDao(), playerInPlaylist);
			}
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void destroyPlayerAndUpdateDatabase(MediaPlayerDataDao dao, EnhancedMediaPlayer player)
	{
		this.removeSoundFromDatabase(dao, player.getMediaPlayerData());
		player.destroy(true);
	}

	private void removeSoundFromDatabase(MediaPlayerDataDao dao, MediaPlayerData playerData)
	{
		if (playerData.getId() != null)
			dao.delete(playerData);
		else
		{
			List<MediaPlayerData> playersInDatabase = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerData.getPlayerId())).list();
			dao.deleteInTx(playersInDatabase);
		}
	}

	private EnhancedMediaPlayer searchInPlaylistForId(String playerId)
	{
		return this.searchInListForId(playerId, playlist);
	}

	private EnhancedMediaPlayer searchInSoundsForId(String playerId)
	{
		Set<String> soundSheets = sounds.keySet();
		for (String soundSheet : soundSheets)
		{
			List<EnhancedMediaPlayer> playersInSoundSheet = sounds.get(soundSheet);
			EnhancedMediaPlayer player = this.searchInListForId(playerId, playersInSoundSheet);
			if (player != null)
				return player;
		}
		return null;
	}

	private EnhancedMediaPlayer searchInListForId(String playerId, List<EnhancedMediaPlayer> players)
	{
		for (EnhancedMediaPlayer player : players)
		{
			if (player.getMediaPlayerData().getPlayerId().equals(playerId))
				return player;
		}
		return null;
	}

	public void moveSoundInFragment(String fragmentTag, int from, int to)
	{
		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);

		EnhancedMediaPlayer playerToMove = soundsInFragment.remove(from);
		soundsInFragment.add(to, playerToMove);

		int count = soundsInFragment.size();
		int indexOfSoundsToUpdate = Math.min(from, to); // we need to update all sound after the moved one
		for (int i = indexOfSoundsToUpdate; i < count; i++)
			soundsInFragment.get(i).getMediaPlayerData().setSortOrder(i);
	}

	private class LoadSoundsTask extends LoadTask<MediaPlayerData>
	{
		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			return getSoundsDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
		{
			super.onSuccess(mediaPlayersData);
			for (MediaPlayerData mediaPlayerData : mediaPlayersData)
				addSoundToSounds(createSoundFromRawData(mediaPlayerData));

			this.sendBroadcastLoadingSoundsSuccessful();
		}

		private void sendBroadcastLoadingSoundsSuccessful()
		{
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISHED_LOADING_SOUNDS);
			broadcastManager.sendBroadcast(intent);
		}
	}

	private class LoadPlaylistTask extends LoadTask<MediaPlayerData>
	{
		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			return getPlaylistDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
		{
			super.onSuccess(mediaPlayersData);
			for (MediaPlayerData mediaPlayerData : mediaPlayersData)
				createPlaylistSoundFromPlayerData(mediaPlayerData);

			this.sendBroadcastLoadingPlayListSuccessful();
		}

		private void sendBroadcastLoadingPlayListSuccessful()
		{
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISHED_LOADING_PLAYLIST);
			broadcastManager.sendBroadcast(intent);
		}
	}

	private abstract class LoadTask<T> extends SafeAsyncTask<List<T>>
	{
		@Override
		protected void onSuccess(List<T> ts) throws Exception
		{
			super.onSuccess(ts);
			Logger.d(TAG, "onSuccess: with " + ts.size() + " sounds loaded");
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private class UpdateSoundsTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayers;
		private DaoSession database;
		private MediaPlayerDataDao dao;

		/**
		 * Update stored sound database
		 * @param mediaPlayers map of media players currently loaded in corresponding sound sheets
		 * @param database daoSession to store data
		 */
		public UpdateSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.dao = getSoundsDao();
			this.mediaPlayers = new ArrayList<>();
			for (String fragmentTag : mediaPlayers.keySet())
			{
				List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
				for (EnhancedMediaPlayer player : playersOfFragment)
					this.mediaPlayers.add(player.getMediaPlayerData());
			}
		}

		/**
		 * Update stored playlist database
		 * @param mediaPlayers list of media players currently loaded in playlist
		 * @param database daoSession to store data
		 */
		public UpdateSoundsTask(List<EnhancedMediaPlayer> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.dao = getPlaylistDao();
			this.mediaPlayers = new ArrayList<>();
			for (EnhancedMediaPlayer player : mediaPlayers)
				this.mediaPlayers.add(player.getMediaPlayerData());
		}

		@Override
		public Void call() throws Exception
		{
			this.database.runInTx(new Runnable()
			{
				@Override
				public void run()
				{
					for (MediaPlayerData playerToUpdate : mediaPlayers)
					{
						List<MediaPlayerData> storePlayers = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerToUpdate.getPlayerId())).list();
						if (storePlayers == null || storePlayers.size() == 0)
							dao.insert(playerToUpdate);
						else
						{
							MediaPlayerData storedPlayer = storePlayers.get(0); // the player id should be unique there this will never be greater than one
							updateStorePlayerData(storedPlayer, playerToUpdate);
							dao.update(storedPlayer);
						}
					}
				}
			});
			return null;
		}

		private void updateStorePlayerData(MediaPlayerData storedPlayer, MediaPlayerData newPlayerData)
		{
			storedPlayer.setFragmentTag(newPlayerData.getFragmentTag());
			storedPlayer.setIsInPlaylist(newPlayerData.getIsInPlaylist());
			storedPlayer.setIsLoop(newPlayerData.getIsInPlaylist());
			storedPlayer.setLabel(newPlayerData.getLabel());
			storedPlayer.setTimePosition(newPlayerData.getTimePosition());
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public class Binder extends android.os.Binder
	{
		public MusicService getService()
		{
			Logger.d(TAG, "getService");
			return MusicService.this;
		}
	}

	// Update notifications, according to player state or notification actions

	/**
	 * This is called by greenDao EventBus in case a mediaplayer changed his state
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
			EnhancedMediaPlayer player = searchInPlaylistForId(playerId);
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

	private void removePlayListNotification()
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
		EnhancedMediaPlayer player = getPlayingSoundFromPlaylist();
		if (player == null)
			player = searchInPlaylistForId(correspondingNotification.getPlayerId());

		if (!player.isPlaying() && isServiceBound) // if player stops playing and the service is still bound, we remove the notification
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
			addNotification(getNotificationForSound(searchInSoundsForId(playerId)));
	}

	private boolean updateOrRemovePendingNotification(String playerId)
	{
		PendingSoundNotification correspondingNotification = this.findNotificationForPendingPlayer(playerId);
		if (correspondingNotification == null)
			return false;

		int notificationId = correspondingNotification.getNotificationId();
		EnhancedMediaPlayer player = searchInSoundsForId(playerId);

		if (!player.isPlaying() && isServiceBound) // if player stops playing and the service is still bound, we remove the notification
		{
			this.removeNotificationForPlayer(playerId);
			return true;
		}

		PendingSoundNotificationBuilder builder = new PendingSoundNotificationBuilder(getApplicationContext(), player, notificationId);

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
			if (notification.getNotificationId() == NotificationIds.NOTIFICATION_ID_PLAYLIST)
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
			if (notification.getNotificationId() == NotificationIds.NOTIFICATION_ID_PLAYLIST)
				return notification;
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
					player = searchInPlaylistForId(playerId);
				else
					player = searchInSoundsForId(playerId);
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

			if (!isServiceBound && notifications.size() == 0)
				stopSelf();
		}
	}

}
