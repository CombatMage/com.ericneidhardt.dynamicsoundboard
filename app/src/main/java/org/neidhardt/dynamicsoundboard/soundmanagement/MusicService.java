package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.acra.ACRA;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadTask;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService extends Service
{
	private static final String TAG = MusicService.class.getName();

	private static final String DB_SOUNDS_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	private static final String DB_SOUNDS_PLAYLIST_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private static final String DB_SOUNDS = "db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "db_sounds_playlist";

	private DaoSession dbPlaylist;
	private volatile List<EnhancedMediaPlayer> playlist = new ArrayList<>();
	List<EnhancedMediaPlayer> getPlaylist()
	{
		return playlist;
	}

	private DaoSession dbSounds;
	private volatile Map<String, List<EnhancedMediaPlayer>> sounds = new HashMap<>();
	Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return sounds;
	}

	private Binder binder;
	private NotificationHandler notificationHandler;

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
		this.notificationHandler = new NotificationHandler(this);

		this.initSoundsAndPlayList();
	}

	public void initSoundsAndPlayList()
	{
		this.dbPlaylist = Util.setupDatabase(this.getApplicationContext(), this.getDatabaseNamePlayList());
		this.dbSounds = Util.setupDatabase(this.getApplicationContext(), this.getDatabaseNameSounds());

		SafeAsyncTask task = new LoadSoundsTask(this.dbSounds);
		task.execute();

		task = new LoadPlaylistTask(this.dbPlaylist);
		task.execute();
	}

	private String getDatabaseNameSounds()
	{
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		if (baseName.equals(SoundLayoutsManager.DB_DEFAULT))
			return DB_SOUNDS_DEFAULT;
		return baseName + DB_SOUNDS;
	}

	private String getDatabaseNamePlayList()
	{
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		if (baseName.equals(SoundLayoutsManager.DB_DEFAULT))
			return DB_SOUNDS_PLAYLIST_DEFAULT;
		return baseName + DB_SOUNDS_PLAYLIST;
	}

	@Override
	public void onDestroy()
	{
		Logger.d(TAG, "onDestroy");

		this.notificationHandler.onServiceDestroyed();
		this.clearAndStoreSoundsAndPlayList();

		super.onDestroy();
	}

	public void clearAndStoreSoundsAndPlayList()
	{
		this.storeLoadedSounds();
		this.notificationHandler.dismissAllNotifications();
		this.releaseMediaPlayers();
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
		this.playlist.clear();
		this.sounds.clear();
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

	public List<EnhancedMediaPlayer> getPlayingSoundsFromSoundList()
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

	public EnhancedMediaPlayer getPlayingSoundFromPlaylist()
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
			MediaPlayerDataDao soundsDao = this.dbSounds.getMediaPlayerDataDao();
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
			this.removeSoundFromDatabase(this.dbSounds.getMediaPlayerDataDao(), playerData);
			return null;
		}
	}

	public void addNewSoundToPlaylist(MediaPlayerData playerData)
	{
		MediaPlayerData dataToStore = this.createPlaylistSoundFromPlayerData(playerData);
		if (dataToStore != null)
		{
			MediaPlayerDataDao playlistDao = this.dbPlaylist.getMediaPlayerDataDao();
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
			this.removeSoundFromDatabase(this.dbPlaylist.getMediaPlayerDataDao(), playerData);
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

				this.destroyPlayerAndUpdateDatabase(this.dbPlaylist.getMediaPlayerDataDao(), correspondingPlayerInPlaylist);
			}
			this.destroyPlayerAndUpdateDatabase(this.dbSounds.getMediaPlayerDataDao(), playerToRemove);
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
				this.destroyPlayerAndUpdateDatabase(this.dbPlaylist.getMediaPlayerDataDao(), playerInPlaylist);
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

	public EnhancedMediaPlayer searchForId(String fragmentTag, String playerId)
	{
		if (fragmentTag.equals(Playlist.TAG))
			return this.searchInPlaylistForId(playerId);
		else
			return this.searchInListForId(playerId, this.sounds.get(fragmentTag));
	}

	public EnhancedMediaPlayer searchInPlaylistForId(String playerId)
	{
		return this.searchInListForId(playerId, playlist);
	}

	public EnhancedMediaPlayer searchInSoundsForId(String playerId)
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
		if (players == null)
			return null;
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

	public boolean isServiceBound()
	{
		return this.isServiceBound;
	}

	private class LoadSoundsTask extends LoadTask<MediaPlayerData>
	{
		private DaoSession daoSession;

		public LoadSoundsTask(DaoSession daoSession)
		{
			this.daoSession = daoSession;
		}

		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
			final EventBus bus = EventBus.getDefault();
			for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
			{
				super.postOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						addSoundToSounds(createSoundFromRawData(mediaPlayerData));
						bus.post(new SoundsLoadedEvent());
					}
				});
			}
			return mediaPlayersData;
		}
	}

	private class LoadPlaylistTask extends LoadTask<MediaPlayerData>
	{
		private DaoSession daoSession;

		public LoadPlaylistTask(DaoSession daoSession)
		{
			this.daoSession = daoSession;
		}

		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
			final EventBus bus = EventBus.getDefault();
			for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
			{
				super.postOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						createPlaylistSoundFromPlayerData(mediaPlayerData);
						bus.post(new PlayListLoadedEvent());
					}
				});
			}
			return mediaPlayersData;
		}
	}

	private class UpdateSoundsTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayers;
		private DaoSession database;

		/**
		 * Update stored sound database
		 * @param mediaPlayers map of media players currently loaded in corresponding sound sheets
		 * @param database daoSession to store data
		 */
		public UpdateSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession database)
		{
			this.database = database;
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
					MediaPlayerDataDao dao = database.getMediaPlayerDataDao();
					for (MediaPlayerData playerToUpdate : mediaPlayers)
					{
						List<MediaPlayerData> storePlayers = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerToUpdate.getPlayerId())).list();
						int count = storePlayers.size();
						if (storePlayers == null || count == 0)
							dao.insert(playerToUpdate);
						else if (count == 1)
						{
							MediaPlayerData storedPlayer = storePlayers.get(0); // the player id should be unique so there should be no more than one entry
							updateStorePlayerData(storedPlayer, playerToUpdate);
							dao.update(storedPlayer);
						}
						else
						{
							String message = "More than one matching entry in dao found " + playerToUpdate;
							Logger.e(TAG, message);
							ACRA.getErrorReporter().handleException(new IllegalStateException(message));
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

}
