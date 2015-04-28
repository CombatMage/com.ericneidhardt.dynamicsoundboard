package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.UpdateSoundsTask;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService extends Service
{
	public static final String TAG = MusicService.class.getName();

	static final String DB_SOUNDS_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	static final String DB_SOUNDS_PLAYLIST_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private static final String DB_SOUNDS = "db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "db_sounds_playlist";

	private DaoSession dbPlaylist;
	private volatile List<EnhancedMediaPlayer> playlist = new ArrayList<>();
	public List<EnhancedMediaPlayer> getPlaylist()
	{
		return playlist;
	}

	private DaoSession dbSounds;
	private volatile Map<String, List<EnhancedMediaPlayer>> sounds = new HashMap<>();
	public Map<String, List<EnhancedMediaPlayer>> getSounds()
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

	public boolean isServiceBound()
	{
		return this.isServiceBound;
	}

	@Override
	public void onRebind(Intent intent)
	{
		this.isServiceBound = true;
	}

	@Override
	public void onCreate()
	{
		Logger.d(TAG, "onCreate");

		super.onCreate();

		this.binder = new Binder(this);
		this.notificationHandler = new NotificationHandler(this);
		EventBus.getDefault().register(this, 1);

		this.initSoundsAndPlayList();
	}

	public void initSoundsAndPlayList()
	{
		this.dbPlaylist = Util.setupDatabase(this.getApplicationContext(), getDatabaseNamePlayList());
		this.dbSounds = Util.setupDatabase(this.getApplicationContext(), getDatabaseNameSounds());

		SafeAsyncTask task = new LoadSoundsTask(this.dbSounds);
		task.execute();

		task = new LoadPlaylistTask(this.dbPlaylist);
		task.execute();
	}

	static String getDatabaseNameSounds()
	{
		if (SoundLayoutsManager.getInstance().getActiveSoundLayout().isDefaultLayout())
			return DB_SOUNDS_DEFAULT;
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		return baseName + DB_SOUNDS;
	}

	static String getDatabaseNamePlayList()
	{
		if (SoundLayoutsManager.getInstance().getActiveSoundLayout().isDefaultLayout())
			return DB_SOUNDS_PLAYLIST_DEFAULT;
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		return baseName + DB_SOUNDS_PLAYLIST;
	}

	@Override
	public void onDestroy()
	{
		Logger.d(TAG, "onDestroy");

		EventBus.getDefault().unregister(this);
		this.notificationHandler.onServiceDestroyed();
		this.clearAndStoreSoundsAndPlayList();

		super.onDestroy();
	}

	/**
	 * Dismisses all pending notifications, store current sound layout and release media player resources.
	 */
	public void clearAndStoreSoundsAndPlayList()
	{
		this.storeLoadedSounds();
		this.notificationHandler.dismissAllNotifications();
		this.releaseMediaPlayers();
	}

	/**
	 * Dismisses all pending notifications and clear sound layout and corresponding databases.
	 */
	public void deleteAllSounds()
	{
		this.dbPlaylist.getMediaPlayerDataDao().deleteAll();
		this.dbSounds.getMediaPlayerDataDao().deleteAll();
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

	public void addNewSoundToSoundsAndDatabase(MediaPlayerData playerData)
	{
		String fragmentTag = playerData.getFragmentTag();
		List<EnhancedMediaPlayer> soundInFragment = this.sounds.get(fragmentTag);
		int sortOrder = soundInFragment == null ? 0 : soundInFragment.size();
		playerData.setSortOrder(sortOrder);

		EnhancedMediaPlayer player = this.createSound(playerData);
		if (player == null)
		{
			this.showLoadingMediaPlayerFailed(playerData.getUri());
			return;
		}
		this.addSoundToSounds(player);

		MediaPlayerDataDao soundsDao = this.dbSounds.getMediaPlayerDataDao();
		soundsDao.insert(player.getMediaPlayerData());
	}

	public void addNewSoundToPlaylistAndDatabase(MediaPlayerData playerData)
	{
		EnhancedMediaPlayer player = this.createPlaylistSound(playerData);
		if (player == null)
		{
			this.showLoadingMediaPlayerFailed(playerData.getUri());
			return;
		}
		this.addSoundToPlaylist(player);
		MediaPlayerDataDao playlistDao = this.dbPlaylist.getMediaPlayerDataDao();
		playlistDao.insert(player.getMediaPlayerData()); // it is important to use data returned from createPlaylistSound, because it is a new instance
	}

	/**
	 * Creates an new EnhancedMediaPlayer instance
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private EnhancedMediaPlayer createSound(MediaPlayerData playerData)
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
	/**
	 * Creates an new EnhancedMediaPlayer instance and adds this instance to the playlist.
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private EnhancedMediaPlayer createPlaylistSound(MediaPlayerData playerData)
	{
		try
		{
			return EnhancedMediaPlayer.getInstanceForPlayList(playerData);
		}
		catch (IOException e)
		{
			Logger.d(TAG, playerData.toString()+ " " + e.getMessage());
			this.removeSoundFromDatabase(this.dbPlaylist.getMediaPlayerDataDao(), playerData);
			return null;
		}
	}

	/**
	 * Adds sound to corresponding sound list. If the list is long enough, the players sort order is respected, otherwise it is added to the end of the list
	 * @param player the new player to add
	 */
	private void addSoundToSounds(EnhancedMediaPlayer player)
	{
		if (player == null)
			throw new NullPointerException("cannot add new Player, player is null");
		String fragmentTag = player.getMediaPlayerData().getFragmentTag();
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		Integer index = player.getMediaPlayerData().getSortOrder();
		if (index == null)
			index = 0;

		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);
		int count = soundsInFragment.size();
		if (index <= count) // add item according to sort order
			soundsInFragment.add(index, player);
		else
			soundsInFragment.add(player); // if the list is to short, just append
	}

	private void addSoundToPlaylist(EnhancedMediaPlayer player)
	{
		if (player == null)
			throw new NullPointerException("cannot add new Player to playlist, player is null");
		this.playlist.add(player);
	}

	public void removeSounds(String fragmentTag) {
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
		EnhancedMediaPlayer player = this.searchInSoundsForId(playerId);
		EnhancedMediaPlayer playerInPlaylist = this.searchInPlaylistForId(playerId);

		if (addToPlayList)
		{
			if (playerInPlaylist != null)
				return;

			player.setIsInPlaylist(true);
			addNewSoundToPlaylistAndDatabase(player.getMediaPlayerData());
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
		{
			soundsInFragment.get(i).getMediaPlayerData().setSortOrder(i);
			soundsInFragment.get(i).getMediaPlayerData().setItemWasAltered();
		}
	}

	/**
	 * This is called by greenDao EventBus in case sound loading from MusicService has finished
	 * @param event delivered SoundsLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(SoundsLoadedEvent event)
	{
		MediaPlayerData data = event.getLoadedSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEventMainThread() delivered data is null");
		EnhancedMediaPlayer player = createSound(data);
		if (player == null)
		{
			showLoadingMediaPlayerFailed(data.getUri());
			this.removeSoundFromDatabase(this.dbSounds.getMediaPlayerDataDao(), data);
		}
		else
			addSoundToSounds(player);
	}

	/**
	 * This is called by greenDao EventBus in case loading the playlist from MusicService has finished
	 * @param event delivered PlayListLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(PlayListLoadedEvent event)
	{
		MediaPlayerData data = event.getLoadedSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEventMainThread() delivered data is null");
		EnhancedMediaPlayer player = createSound(data);
		if (player == null)
		{
			showLoadingMediaPlayerFailed(data.getUri());
			this.removeSoundFromDatabase(this.dbPlaylist.getMediaPlayerDataDao(), data);
		}
		else
			addSoundToPlaylist(player);
	}

	private void showLoadingMediaPlayerFailed(String playerUriString)
	{
		String message = this.getResources().getString(R.string.music_service_loading_sound_failed) + " "
				+ FileUtils.getFileNameFromUri(getApplicationContext(), playerUriString);
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static class Binder extends android.os.Binder
	{
		private MusicService service;

		public Binder(MusicService service)
		{
			this.service = service;
		}

		public MusicService getService()
		{
			Logger.d(TAG, "getService");
			return this.service;
		}
	}

}
