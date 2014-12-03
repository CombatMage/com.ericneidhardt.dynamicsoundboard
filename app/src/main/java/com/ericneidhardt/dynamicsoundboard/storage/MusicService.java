package com.ericneidhardt.dynamicsoundboard.storage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService extends Service
{
	private static final String TAG = MusicService.class.getName();

	public static final String ACTION_FINISHED_LOADING_PLAYLIST = "com.ericneidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_PLAYLIST";
	public static final String ACTION_FINISHED_LOADING_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.ACTION_FINISHED_LOADING_SOUNDS";

	public static final String KEY_SOUNDSHEETS = "com.ericneidhardt.dynamicsoundboard.storage.KEY_SOUNDSHEETS";

	private static final String DB_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private DaoSession dbPlaylist;
	private List<EnhancedMediaPlayer> playList;

	public List<EnhancedMediaPlayer> getPlayList()
	{
		return playList;
	}

	private DaoSession dbSounds;
	private Map<String, List<EnhancedMediaPlayer>> sounds;

	public Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return sounds;
	}

	private Binder binder;

	@Override
	public IBinder onBind(Intent intent)
	{
		return this.binder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		this.binder = new Binder();

		this.playList = new ArrayList<EnhancedMediaPlayer>();
		this.sounds = new HashMap<String, List<EnhancedMediaPlayer>>();

		this.dbPlaylist = Util.setupDatabase(this.getApplicationContext(), DB_SOUNDS_PLAYLIST);
		this.dbSounds = Util.setupDatabase(this.getApplicationContext(), DB_SOUNDS);

		SafeAsyncTask task = new LoadSoundsTask();
		//task.execute();

		task = new LoadPlaylistTask();
		//task.execute();
	}

	@Override
	public void onDestroy()
	{
		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, dbSounds);
		//task.execute();

		task = new UpdateSoundsTask(this.playList, dbPlaylist);
		//task.execute();

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
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

	public void addSound(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = new EnhancedMediaPlayer(this.getApplicationContext(), playerData);
			if (this.sounds.get(playerData.getFragmentTag()) == null)
				this.sounds.put(playerData.getFragmentTag(), new ArrayList<EnhancedMediaPlayer>(asList(player)));
			else
				this.sounds.get(playerData.getFragmentTag()).add(player);
		} catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
		}
	}

	public void addSoundToPlaylist(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = EnhancedMediaPlayer.getInstanceForPlayList(this.getApplicationContext(), playerData);
			this.playList.add(player);
		} catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
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

				correspondingPlayerInPlaylist.destroy();
			}
			playerToRemove.destroy();
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
				playerInPlaylist.destroy();
			}
		} catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
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

	public void notifySoundSheetList()
	{
		// TODO
		/*
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list*/
	}

	public void notifyFragment(String fragmentTag)
	{
		// TODO
		/*
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);

		SoundSheetFragment fragment = (SoundSheetFragment) this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged(true);

		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list*/
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
				addSound(mediaPlayerData);

			this.sendBroadcastLoadingSoundsSuccessful();
		}

		private void sendBroadcastLoadingSoundsSuccessful()
		{
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISHED_LOADING_SOUNDS);
			getApplicationContext().sendBroadcast(intent);
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
				addSoundToPlaylist(mediaPlayerData);

			this.sendBroadcastLoadingPlayListSuccessful();
		}

		private void sendBroadcastLoadingPlayListSuccessful()
		{
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISHED_LOADING_PLAYLIST);
			getApplicationContext().sendBroadcast(intent);
		}
	}

	private abstract class LoadTask<T> extends SafeAsyncTask<List<T>>
	{
		@Override
		protected void onSuccess(List<T> ts) throws Exception
		{
			super.onSuccess(ts);
			Logger.d(TAG, "onSuccess: with " + ts.size());
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
					database.getMediaPlayerDataDao().deleteAll();
					database.getMediaPlayerDataDao().insertInTx(mediaPlayers);
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
			return MusicService.this;
		}
	}
}
