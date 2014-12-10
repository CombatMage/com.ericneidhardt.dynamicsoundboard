package com.ericneidhardt.dynamicsoundboard.soundmanagement;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.SoundPlayingNotification;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<EnhancedMediaPlayer> playList = new ArrayList<EnhancedMediaPlayer>();
	List<EnhancedMediaPlayer> getPlayList()
	{
		return playList;
	}

	private DaoSession dbSounds;
	private Map<String, List<EnhancedMediaPlayer>> sounds = new HashMap<String, List<EnhancedMediaPlayer>>();
	Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return sounds;
	}

	private LocalBroadcastManager broadcastManager;
	private BroadcastReceiver soundStateChangedReceiver;
	private BroadcastReceiver notificationActionReceiver;
	private Binder binder;

	private NotificationManager notificationManager;
	private SoundPlayingNotification notification;

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

		this.soundStateChangedReceiver = new SoundStateChangeReceiver();
		this.notificationActionReceiver = new NotificationActionReceiver();

		this.registerReceiver(this.notificationActionReceiver, SoundPlayingNotification.getNotificationIntentFilter());
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
		this.notificationManager.cancel(SoundPlayingNotification.NOTIFICATION_ID);
		this.notification = null;
	}

	public void onActivityClosed()
	{
		Logger.d(TAG, "onActivityClosed");
		int nrPlayingSounds = this.getCurrentlyPlayingSounds().size();
		if (nrPlayingSounds == 0)
			this.stopSelf();
		else
			this.showNotification(nrPlayingSounds);
	}

	private void showNotification(int nrPlayingSounds)
	{
		if (this.notification == null)
			this.notification = new SoundPlayingNotification(this.getApplicationContext());

		this.notification.setTitle(nrPlayingSounds);
		this.notificationManager.notify(SoundPlayingNotification.NOTIFICATION_ID, this.notification.build());
	}

	public void storeLoadedSounds()
	{
		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, dbSounds);
		task.execute();

		task = new UpdateSoundsTask(this.playList, dbPlaylist);
		task.execute();
	}

	public List<EnhancedMediaPlayer> getCurrentlyPlayingSounds()
	{
		List<EnhancedMediaPlayer> currentlyPlayingSounds = new ArrayList<EnhancedMediaPlayer>();
		for (EnhancedMediaPlayer sound : this.playList)
		{
			if (sound.isPlaying())
				currentlyPlayingSounds.add(sound);
		}
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

	public void addNewSoundToServiceAndDatabase(MediaPlayerData playerData)
	{
		if (this.addLoadedSound(playerData))
			this.dbSounds.getMediaPlayerDataDao().insert(playerData);
	}

	private boolean addLoadedSound(MediaPlayerData playerData)
	{
		try
		{
			String fragmentTag = playerData.getFragmentTag();
			EnhancedMediaPlayer player = new EnhancedMediaPlayer(this.getApplicationContext(), playerData);

			if (this.sounds.get(fragmentTag) == null)
			{
				List<EnhancedMediaPlayer> soundListForFragment = new ArrayList<EnhancedMediaPlayer>();
				soundListForFragment.add(player);
				this.sounds.put(fragmentTag, soundListForFragment);
			}
			else
				this.sounds.get(fragmentTag).add(player);

			return true;
		} catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			this.removeSoundFromDatabase(dbSounds, playerData);
			return false;
		}
	}

	public void addNewSoundToPlaylist(MediaPlayerData playerData)
	{
		if (this.addLoadedSoundToPlaylist(playerData))
			this.dbPlaylist.getMediaPlayerDataDao().insert(playerData);
	}

	private boolean addLoadedSoundToPlaylist(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = EnhancedMediaPlayer.getInstanceForPlayList(this.getApplicationContext(), playerData);
			this.playList.add(player);
			return true;
		} catch (IOException e)
		{
			Logger.d(TAG, playerData.toString()+ " " + e.getMessage());
			this.removeSoundFromDatabase(dbPlaylist, playerData);
			return false;
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

		List<EnhancedMediaPlayer> copyList = new ArrayList<EnhancedMediaPlayer>(soundsToRemove.size());
		copyList.addAll(soundsToRemove); // this is done to prevent concurrent modification exception

		for (EnhancedMediaPlayer playerToRemove : copyList)
		{
			MediaPlayerData data = playerToRemove.getMediaPlayerData();
			this.sounds.get(data.getFragmentTag()).remove(playerToRemove);

			if (data.getIsInPlaylist())
			{
				EnhancedMediaPlayer correspondingPlayerInPlaylist = this.findInPlaylist(data.getPlayerId());
				this.playList.remove(correspondingPlayerInPlaylist);

				this.destroyPlayerAndUpdateDatabase(this.dbPlaylist, correspondingPlayerInPlaylist);
			}
			this.destroyPlayerAndUpdateDatabase(this.dbSounds, playerToRemove);
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
			EnhancedMediaPlayer player = this.findInSounds(playerId);
			EnhancedMediaPlayer playerInPlaylist = this.findInPlaylist(playerId);

			if (addToPlayList)
			{
				if (playerInPlaylist != null)
					return;

				player.setIsInPlaylist(true);
				playerInPlaylist = EnhancedMediaPlayer.getInstanceForPlayList(this.getApplicationContext(), player.getMediaPlayerData());
				this.playList.add(playerInPlaylist);
			} else
			{
				if (playerInPlaylist == null)
					return;

				player.setIsInPlaylist(false);
				this.playList.remove(playerInPlaylist);
				this.destroyPlayerAndUpdateDatabase(this.dbPlaylist, playerInPlaylist);
			}
		} catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void destroyPlayerAndUpdateDatabase(DaoSession daoSession, EnhancedMediaPlayer player)
	{
		this.removeSoundFromDatabase(daoSession, player.getMediaPlayerData());
		player.destroy();
	}

	private void removeSoundFromDatabase(DaoSession daoSession, MediaPlayerData playerData)
	{
		daoSession.getMediaPlayerDataDao().delete(playerData);
	}

	private EnhancedMediaPlayer findInPlaylist(String playerId)
	{
		for (EnhancedMediaPlayer player : this.playList)
		{
			if (player.getMediaPlayerData().getPlayerId().equals(playerId))
				return player;
		}
		return null;
	}

	private EnhancedMediaPlayer findInSounds(String playerId)
	{
		for (String fragmentTag : this.sounds.keySet())
		{
			if (this.sounds.get(fragmentTag) == null)
				continue;
			for (EnhancedMediaPlayer player : this.sounds.get(fragmentTag))
			{
				if (player.getMediaPlayerData().getPlayerId().equals(playerId))
					return player;
			}
		}
		return null;
	}

	private class LoadSoundsTask extends LoadTask<MediaPlayerData>
	{
		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			return dbSounds.getMediaPlayerDataDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
		{
			super.onSuccess(mediaPlayersData);
			for (MediaPlayerData mediaPlayerData : mediaPlayersData)
				addLoadedSound(mediaPlayerData);

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
			return dbPlaylist.getMediaPlayerDataDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
		{
			super.onSuccess(mediaPlayersData);
			for (MediaPlayerData mediaPlayerData : mediaPlayersData)
				addLoadedSoundToPlaylist(mediaPlayerData);

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

		public UpdateSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			for (String fragmentTag : mediaPlayers.keySet())
			{
				List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
				for (EnhancedMediaPlayer player : playersOfFragment)
					this.mediaPlayers.add(player.getMediaPlayerData());
			}
		}

		public UpdateSoundsTask(List<EnhancedMediaPlayer> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
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
					MediaPlayerDataDao dao = database.getMediaPlayerDataDao();
					for (MediaPlayerData playerToUpdate : mediaPlayers)
					{
						List<MediaPlayerData> storePlayers = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerToUpdate.getPlayerId())).list();
						if (storePlayers == null || storePlayers.size() == 0)
							dao.insert(playerToUpdate);
						// else we could update the existing dao, but this is currently not necessary
					}
				}
			});
			return null;
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
			Logger.d(TAG, "SoundStateChangeReceiver.onReceive" + intent);

			// TODO update notification
		}
	}

	private class NotificationActionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Logger.d(TAG, "NotificationActionReceiver.onReceive" + intent);

			String action = intent.getAction();
			if (action == null)
				return;

			if (action.equals(SoundPlayingNotification.ACTION_DISMISS))
			{
				stopSelf();
				return;
			}
				// TODO update notification and sounds
		}
	}

}
