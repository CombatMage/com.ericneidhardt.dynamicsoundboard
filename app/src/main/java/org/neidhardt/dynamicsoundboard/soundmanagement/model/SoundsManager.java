package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.soundactivity.events.OnSoundsLoadedEventListener;
import org.neidhardt.dynamicsoundboard.soundactivity.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.SoundLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.UpdateSoundsTask;
import roboguice.util.SafeAsyncTask;

import java.io.IOException;
import java.util.*;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class SoundsManager
	implements
		SoundsDataAccess,
		SoundsDataStorage,
		SoundsDataUtil,
		MediaPlayerEventListener,
		OnSoundsLoadedEventListener
{
	private static final String TAG = SoundsManager.class.getName();

	EventBus eventBus;

	private DaoSession dbPlaylist;
	private DaoSession dbSounds;

	private Map<String, List<EnhancedMediaPlayer>> sounds;
	private List<EnhancedMediaPlayer> playlist;
	private Set<EnhancedMediaPlayer> currentlyPlayingSounds;

	private boolean isInitDone;

	public SoundsManager()
	{
		this.isInitDone = false;

		this.eventBus = EventBus.getDefault();
		this.init();
	}

	private DaoSession getDbSounds()
	{
		if (this.dbSounds == null)
			this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());
		return this.dbSounds;
	}

	private DaoSession getDbPlaylist()
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());
		return this.dbPlaylist;
	}

	@Override
	public void init()
	{
		if (this.isInitDone)
			throw new IllegalStateException(TAG + ": ini() was called, but SoundsManager was already initialized");

		this.sounds = new HashMap<>();
		this.playlist = new ArrayList<>();
		this.currentlyPlayingSounds = new HashSet<>();

		this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNamePlayList());
		this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());

		SafeAsyncTask task = new LoadSoundsTask(this.dbSounds);
		task.execute();

		task = new LoadPlaylistTask(this.dbPlaylist);
		task.execute();
	}

	@Override
	public boolean isInit()
	{
		return this.isInitDone;
	}

	@Override
	public void registerOnEventBus()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.registerSticky(this, 1);
	}

	@Override
	public void unregisterOnEventBus()
	{
		this.eventBus.unregister(this);
	}

	@Override
	public void writeCacheBackAndRelease()
	{
		this.isInitDone = false;

		this.storeLoadedSounds();
		this.releaseMediaPlayers();
	}

	private void storeLoadedSounds()
	{
		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, this.getDbSounds());
		task.execute();

		task = new UpdateSoundsTask(this.playlist, getDbPlaylist());
		task.execute();
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
	public Set<EnhancedMediaPlayer> getCurrentlyPlayingSounds()
	{
		return this.currentlyPlayingSounds;
	}

	@Override
	public List<EnhancedMediaPlayer> getPlaylist()
	{
		return this.playlist;
	}

	@Override
	public Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return this.sounds;
	}

	@Override
	public List<EnhancedMediaPlayer> getSoundsInFragment(String fragmentTag)
	{
		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);
		if (soundsInFragment == null)
		{
			soundsInFragment = new ArrayList<>();
			this.sounds.put(fragmentTag, soundsInFragment);
		}
		return soundsInFragment;
	}

	@Override
	public EnhancedMediaPlayer getSoundById(String fragmentTag, String playerId)
	{
		if (fragmentTag.equals(Playlist.TAG))
			return SoundsManagerUtil.searchInListForId(playerId, playlist);
		else
			return SoundsManagerUtil.searchInListForId(playerId, this.sounds.get(fragmentTag));
	}

	@Override
	public void toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		EnhancedMediaPlayer player = SoundsManagerUtil.searchInMapForId(playerId, this.sounds);
		EnhancedMediaPlayer playerInPlaylist = SoundsManagerUtil.searchInListForId(playerId, playlist);

		if (addToPlayList)
		{
			if (playerInPlaylist != null)
				return;

			if (player != null)
			{
				player.setIsInPlaylist(true);
				this.addSoundToPlayList(player.getMediaPlayerData());
			}
		}
		else
		{
			if (playerInPlaylist == null)
				return;

			if (player != null)
				player.setIsInPlaylist(false);

			this.playlist.remove(playerInPlaylist);

			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerInPlaylist.getMediaPlayerData());
			playerInPlaylist.destroy(true);
		}
	}

	@Override
	public void addSoundToPlayList(MediaPlayerData data)
	{
		EnhancedMediaPlayer player = createPlaylistSound(data);
		if (player == null)
		{
			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
		}
		else
		{
			this.playlist.add(player);

			MediaPlayerDataDao playlistDap = this.getDbPlaylist().getMediaPlayerDataDao();
			playlistDap.insert(player.getMediaPlayerData());
		}
		this.eventBus.post(new PlaylistChangedEvent());
	}

	@Override
	public void addSoundToSounds(EnhancedMediaPlayer player)
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

		this.eventBus.post(new SoundsChangedEvent());
	}

	@Override
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
				EnhancedMediaPlayer correspondingPlayerInPlaylist = SoundsManagerUtil.searchInListForId(data.getPlayerId(), this.playlist);
				this.playlist.remove(correspondingPlayerInPlaylist);

				this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), correspondingPlayerInPlaylist.getMediaPlayerData());
				correspondingPlayerInPlaylist.destroy(true);
			}
			this.removeSoundFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerToRemove.getMediaPlayerData());
			playerToRemove.destroy(true);
		}

		this.eventBus.post(new SoundsChangedEvent());
	}

	@Override
	public void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove)
	{
		for (EnhancedMediaPlayer player : soundsToRemove)
			this.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), false);

		this.eventBus.post(new SoundsChangedEvent());
	}

	@Override
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
			Logger.d(TAG, playerData.toString() + " " + e.getMessage());
			this.removeSoundFromDatabase(this.dbPlaylist.getMediaPlayerDataDao(), playerData);
			return null;
		}
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

	@Override
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		EnhancedMediaPlayer player = event.getPlayer();
		if (player.isPlaying())
			this.currentlyPlayingSounds.add(player);
		else
			this.currentlyPlayingSounds.remove(player);
	}

	@Override
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		this.currentlyPlayingSounds.remove(event.getPlayer());
	}

	@Override
	public void onEventMainThread(SoundLoadedEvent event)
	{
		MediaPlayerData data = event.getNewSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEvent() delivered data is null " + event);

		if (this.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = createSound(data);
		if (player == null)
		{
			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
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

	@Override
	public void onEventMainThread(PlaylistLoadedEvent event)
	{
		MediaPlayerData data = event.getLoadedSoundData();
		if (data == null)
			throw new NullPointerException(TAG + ": onEvent() delivered data is null");
		EnhancedMediaPlayer player = createPlaylistSound(data);
		if (player == null)
		{
			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
		}
		else
		{
			this.playlist.add(player);
			if (!event.isLoadFromDatabase()) // if the player was not loaded from the database, we need to add it to the database
			{
				MediaPlayerDataDao playlistDap = this.getDbPlaylist().getMediaPlayerDataDao();
				playlistDap.insert(player.getMediaPlayerData());
			}
		}
	}
}
