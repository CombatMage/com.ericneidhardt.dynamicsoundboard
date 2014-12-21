package com.ericneidhardt.dynamicsoundboard.soundmanagement;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import com.ericneidhardt.dynamicsoundboard.notifications.Constants;
import com.ericneidhardt.dynamicsoundboard.notifications.PendingSoundNotification;
import com.ericneidhardt.dynamicsoundboard.notifications.PendingSoundNotificationBuilder;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService extends Service
{
	private static final String TAG = MusicService.class.getName();

	public static final String ACTION_FINISHED_LOADING_PLAYLIST = "com.ericneidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_PLAYLIST";
	public static final String ACTION_FINISHED_LOADING_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_SOUNDS";

	private static final String DB_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

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
	private BroadcastReceiver soundStateChangedReceiver;
	private BroadcastReceiver notificationActionReceiver;
	private Binder binder;

	private NotificationManager notificationManager;
	private List<PendingSoundNotification> notifications;

	@Override
	public IBinder onBind(Intent intent)
	{
		return this.binder;
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

		this.soundStateChangedReceiver = new SoundStateChangeReceiver();
		this.notificationActionReceiver = new NotificationActionReceiver();

		this.registerReceiver(this.notificationActionReceiver, PendingSoundNotificationBuilder.getNotificationIntentFilter());
		this.broadcastManager.registerReceiver(this.soundStateChangedReceiver, EnhancedMediaPlayer.getMediaPlayerIntentFilter());

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
		this.unregisterReceiver(this.notificationActionReceiver);
		this.broadcastManager.unregisterReceiver(this.soundStateChangedReceiver);
		this.storeLoadedSounds();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Logger.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	public void onActivityResumed()
	{
		for (PendingSoundNotification notification : this.notifications)
		{
			int notificationId = notification.getNotificationId();
			this.notificationManager.cancel(notificationId);
		}
		this.notifications.clear();
	}

	public void onActivityClosed()
	{
		Logger.d(TAG, "onActivityClosed");
		List<EnhancedMediaPlayer> pendingPlayers = this.getCurrentlyPlayingSounds();
		if (pendingPlayers.size() == 0)
			this.stopSelf();
		else
			this.showNotifications();
	}

	private void showNotifications()
	{
		List<EnhancedMediaPlayer> pendingSounds = this.getPlayingSoundsFromSoundList();
		for (EnhancedMediaPlayer player : pendingSounds)
		{
			PendingSoundNotificationBuilder builder = new PendingSoundNotificationBuilder(this.getApplicationContext(), player);
			this.addNotification(builder);
		}

		EnhancedMediaPlayer player = this.getPlayingSoundFromPlaylist();
		if (player != null)
		{
			PendingSoundNotificationBuilder builder = this.getNotificationForPlaylist(player);
			this.addNotification(builder);
		}
	}

	private PendingSoundNotificationBuilder getNotificationForPlaylist(EnhancedMediaPlayer player)
	{
		return new PendingSoundNotificationBuilder(
				this.getApplicationContext(),
				player, Constants.NOTIFICATION_ID_PLAYLIST,
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
		MediaPlayerData dataToStore = this.createSoundFromRawData(playerData);
		if (dataToStore != null)
		{
			MediaPlayerDataDao soundsDao = this.getSoundsDao();
			soundsDao.insert(dataToStore);
		}
	}

	/**
	 * Creates an new EnhancedMediaPlayer instance and adds this instance to the list of loaded sounds.
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private MediaPlayerData createSoundFromRawData(MediaPlayerData playerData)
	{
		try
		{
			String fragmentTag = playerData.getFragmentTag();
			EnhancedMediaPlayer player = new EnhancedMediaPlayer(this.getApplicationContext(), playerData);

			if (this.sounds.get(fragmentTag) == null)
			{
				List<EnhancedMediaPlayer> soundListForFragment = new ArrayList<>();
				soundListForFragment.add(player);
				this.sounds.put(fragmentTag, soundListForFragment);
			}
			else
				this.sounds.get(fragmentTag).add(player);

			return player.getMediaPlayerData();
		} catch (IOException e)
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
			EnhancedMediaPlayer player = EnhancedMediaPlayer.getInstanceForPlayList(this.getApplicationContext(), playerData);
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
				playerInPlaylist = EnhancedMediaPlayer.getInstanceForPlayList(this.getApplicationContext(), player.getMediaPlayerData());
				this.playlist.add(playerInPlaylist);
			} else
			{
				if (playerInPlaylist == null)
					return;

				if (player != null)
					player.setIsInPlaylist(false);

				this.playlist.remove(playerInPlaylist);
				this.destroyPlayerAndUpdateDatabase(this.getPlaylistDao(), playerInPlaylist);
			}
		} catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void destroyPlayerAndUpdateDatabase(MediaPlayerDataDao dao, EnhancedMediaPlayer player)
	{
		this.removeSoundFromDatabase(dao, player.getMediaPlayerData());
		player.destroy();
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

	private EnhancedMediaPlayer searchInSoundsAndPlaylistForId(String playerId)
	{
		EnhancedMediaPlayer player = this.searchInPlaylistForId(playerId);
		if (player == null)
			player = this.searchInSoundsForId(playerId);
		return player;
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
				createSoundFromRawData(mediaPlayerData);

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

	private class SoundStateChangeReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Logger.d(TAG, "SoundStateChangeReceiver.onReceive " + intent);

			String action = intent.getAction();
			String playerId = intent.getStringExtra(Constants.KEY_PLAYER_ID);

			if (action == null || playerId == null)
				return;

			EnhancedMediaPlayer player = searchInPlaylistForId(playerId);
			if (player != null)
				this.updatePendingPlaylistNotification();
			else
				this.updatePendingNotification(playerId);
		}

		private void updatePendingPlaylistNotification()
		{
			PendingSoundNotification correspondingNotification = this.findNotificationById(Constants.NOTIFICATION_ID_PLAYLIST);
			if (correspondingNotification == null)
				return;
			int notificationId = correspondingNotification.getNotificationId();

			EnhancedMediaPlayer player = getPlayingSoundFromPlaylist();
			if (player == null)
				player = searchInPlaylistForId(correspondingNotification.getPlayerId());
			PendingSoundNotificationBuilder builder = getNotificationForPlaylist(player);

			correspondingNotification.setPlayerId(player.getMediaPlayerData().getPlayerId());
			correspondingNotification.setNotification(builder.build());
			notificationManager.notify(notificationId, correspondingNotification.getNotification());
		}

		private void updatePendingNotification(String playerId)
		{
			PendingSoundNotification correspondingNotification = this.findNotificationForPendingPlayer(playerId);
			if (correspondingNotification == null)
				return;

			int notificationId = correspondingNotification.getNotificationId();
			EnhancedMediaPlayer player = searchInSoundsForId(playerId);
			PendingSoundNotificationBuilder builder = new PendingSoundNotificationBuilder(getApplicationContext(), player, notificationId);

			correspondingNotification.setNotification(builder.build());
			notificationManager.notify(notificationId, correspondingNotification.getNotification());
		}

		private PendingSoundNotification findNotificationForPendingPlayer(String playerId)
		{
			for (PendingSoundNotification notification : notifications)
			{
				if (notification.getPlayerId().equals(playerId))
					return notification;
			}
			return null;
		}

		private PendingSoundNotification findNotificationById(int notificationId)
		{
			for (PendingSoundNotification notification : notifications)
			{
				if (notification.getNotificationId() == notificationId)
					return notification;
			}
			return null;
		}
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

			String playerId = intent.getStringExtra(Constants.KEY_PLAYER_ID);
			if (playerId == null)
				return;

			if (action.equals(Constants.ACTION_DISMISS))
			{
				int notificationId = intent.getIntExtra(Constants.KEY_NOTIFICATION_ID, 0);
				this.dismissPendingMediaPlayer(notificationId, playerId);
			}
			else
			{
				EnhancedMediaPlayer player = searchInSoundsAndPlaylistForId(playerId);
				switch (action)
				{
					case Constants.ACTION_PAUSE:
						player.pauseSound();
						break;
					case Constants.ACTION_STOP:
						player.stopSound();
						break;
					case Constants.ACTION_PLAY:
						player.playSound();
						break;
				}
			}
		}

		private void dismissPendingMediaPlayer(int notificationId, String playerId)
		{
			PendingSoundNotification notificationToRemove = null;
			for (PendingSoundNotification notification : notifications)
			{
				if (notification.getNotificationId() == notificationId)
					notificationToRemove = notification;
			}
			if (notificationToRemove != null)
				notifications.remove(notificationToRemove);

			if (notificationId == Constants.NOTIFICATION_ID_PLAYLIST)
				this.stopAllSoundsInPlaylist();
			else
				this.stopSound(playerId);

			if (notifications.size() == 0)
				stopSelf();
		}

		private void stopAllSoundsInPlaylist()
		{
			for (EnhancedMediaPlayer player : playlist)
				player.stopSound();
		}

		private void stopSound(String playerId)
		{
			EnhancedMediaPlayer player = searchInSoundsAndPlaylistForId(playerId);
			if (player != null)
				player.pauseSound();
		}
	}

}
