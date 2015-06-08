package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.AddNewSoundEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.UpdateSoundsTask;
import roboguice.util.SafeAsyncTask;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 01.12.2014.
 */
public class MusicService
		extends
			Service
{
	public static final String TAG = MusicService.class.getName();

	static final String DB_SOUNDS_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	static final String DB_SOUNDS_PLAYLIST_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private static final String DB_SOUNDS = "db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "db_sounds_playlist";

	private Handler handler = new Handler();

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

	private Set<EnhancedMediaPlayer> currentlyPlayingSounds = new HashSet<>();
	public Set<EnhancedMediaPlayer> getCurrentlyPlayingSounds()
	{
		return Collections.unmodifiableSet(this.currentlyPlayingSounds);
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
		EventBus.getDefault().registerSticky(this, 1);

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

	private DaoSession getDbSounds()
	{
		if (this.dbSounds == null)
			this.dbSounds = Util.setupDatabase(this.getApplicationContext(), getDatabaseNameSounds());
		return this.dbSounds;
	}

	private DaoSession getDbPlaylist()
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = Util.setupDatabase(this.getApplicationContext(), getDatabaseNameSounds());
		return this.dbPlaylist;
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
		this.getDbPlaylist().getMediaPlayerDataDao().deleteAll();
		this.getDbSounds().getMediaPlayerDataDao().deleteAll();
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
		if (this.currentlyPlayingSounds.size() == 0)
			this.stopSelf();
	}

	public void storeLoadedSounds()
	{
		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, this.getDbSounds());
		task.execute();

		task = new UpdateSoundsTask(this.playlist, getDbPlaylist());
		task.execute();
	}

	public EnhancedMediaPlayer getPlayingSoundFromPlaylist()
	{
		for (EnhancedMediaPlayer sound : this.currentlyPlayingSounds)
		{
			if (sound.getMediaPlayerData().getFragmentTag().equals(Playlist.TAG))
				return sound;
		}
		return null;
	}

	/**
	 * Creates an new EnhancedMediaPlayer instance
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	private EnhancedMediaPlayer createSound(MediaPlayerData playerData)
	{
		int itemsInFragment = this.sounds.get(playerData.getFragmentTag()) != null ? this.sounds.get(playerData.getFragmentTag()).size() : 0;
		if (playerData.getSortOrder() == null || playerData.getSortOrder() > itemsInFragment)
			playerData.setSortOrder(itemsInFragment);
		try
		{
			return new EnhancedMediaPlayer(playerData);
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			this.removeSoundFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerData);
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

				this.destroyPlayerAndUpdateDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), correspondingPlayerInPlaylist);
			}
			this.destroyPlayerAndUpdateDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerToRemove);
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
			this.onEvent(new PlaylistLoadedEvent(player.getMediaPlayerData(), false));
		}
		else
		{
			if (playerInPlaylist == null)
				return;

			if (player != null)
				player.setIsInPlaylist(false);

			this.playlist.remove(playerInPlaylist);
			this.destroyPlayerAndUpdateDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerInPlaylist);
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
	 * This is called by greenRobot EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		EnhancedMediaPlayer player = event.getPlayer();
		if (player.isPlaying())
			this.currentlyPlayingSounds.add(player);
		else
			this.currentlyPlayingSounds.remove(player);
	}

	/**
	 * This is called by greenRobot EventBus in case sound loading from MusicService has finished
	 * @param event delivered AddNewSoundEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(AddNewSoundEvent event)
	{
		MediaPlayerData data = event.getNewSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEvent() delivered data is null " + event);

		if (this.searchForId(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = createSound(data);
		if (player == null)
		{
			showLoadingMediaPlayerFailed(data.getUri());
			this.removeSoundFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), data);
		}
		else
		{
			addSoundToSounds(player);

			if (!event.isLoadFromDatabase()) // if the player was not loaded from the database, we need to add it to the database
			{
				MediaPlayerDataDao soundsDao = this.getDbSounds().getMediaPlayerDataDao();
				soundsDao.insert(player.getMediaPlayerData());
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case a sound sheet was removed.
	 * playlist entries.
	 * @param event delivered SoundSheetsRemovedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundSheetsRemovedEvent event)
	{
		SoundSheet soundSheet = event.getRemovedSoundSheet();
		if (soundSheet == null)
			throw new NullPointerException(TAG + ": onEvent() delivered Data is null " + event);

		List<EnhancedMediaPlayer> soundsInSoundSheet = this.getSounds().get(soundSheet.getFragmentTag());
		this.removeSounds(soundsInSoundSheet);
	}

	/**
	 * This is called by greenRobot EventBus in case loading the playlist from MusicService has finished
	 * @param event delivered PlaylistLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(PlaylistLoadedEvent event)
	{
		MediaPlayerData data = event.getLoadedSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEvent() delivered data is null");
		EnhancedMediaPlayer player = createPlaylistSound(data);
		if (player == null)
		{
			showLoadingMediaPlayerFailed(data.getUri());
			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), data);
		}
		else
		{
			addSoundToPlaylist(player);

			if (!event.isLoadFromDatabase()) // if the player was not loaded from the database, we need to add it to the database
			{
				MediaPlayerDataDao playlistDap = this.getDbPlaylist().getMediaPlayerDataDao();
				playlistDap.insert(player.getMediaPlayerData());
			}
		}

		EventBus.getDefault().post(new PlaylistChangedEvent());
	}

	private void showLoadingMediaPlayerFailed(final String playerUriString)
	{
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				String message = getResources().getString(R.string.music_service_loading_sound_failed) + " "
						+ FileUtils.getFileNameFromUri(getApplicationContext(), playerUriString);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
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
