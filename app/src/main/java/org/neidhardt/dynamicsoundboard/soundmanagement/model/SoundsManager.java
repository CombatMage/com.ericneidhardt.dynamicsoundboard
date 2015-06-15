package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.UpdateSoundsTask;
import roboguice.util.SafeAsyncTask;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class SoundsManager
	implements
		SoundsDataAccess,
		SoundsDataStorage,
		SoundsDataUtil
{
	private static final String TAG = SoundsManager.class.getName();

	EventBus eventBus;

	private DaoSession dbPlaylist;
	private DaoSession dbSounds;

	private Map<String, List<EnhancedMediaPlayer>> sounds;
	private List<EnhancedMediaPlayer> playlist;
	private Set<EnhancedMediaPlayer> currentlyPlayingSounds;

	public SoundsManager()
	{
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
	public void writeCacheBack()
	{
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
	public List<EnhancedMediaPlayer> getPlayList()
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
				this.addSoundsToPlayList(player.getMediaPlayerData());
			}
		}
		else
		{
			if (playerInPlaylist == null)
				return;

			if (player != null)
				player.setIsInPlaylist(false);

			this.playlist.remove(playerInPlaylist);
			playerInPlaylist.destroy(true);
			this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerInPlaylist.getMediaPlayerData());
		}
	}

	@Override
	public void addSoundsToPlayList(MediaPlayerData data)
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
	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		// TODO
	}

	@Override
	public void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove)
	{
		// TODO
	}

	@Override
	public void moveSoundInFragment(String fragmentTag, int from, int to)
	{
		// TODO
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
}
