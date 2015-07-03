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
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistFromDatabaseTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromDatabaseTask;
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
		SoundsDataUtil
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

	public DaoSession getDbSounds()
	{
		if (this.dbSounds == null)
			this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());
		return this.dbSounds;
	}


	public DaoSession getDbPlaylist()
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNamePlayList());
		return this.dbPlaylist;
	}

	@Override
	public void init()
	{
		if (this.isInitDone)
			throw new IllegalStateException(TAG + ": init() was called, but SoundsManager was already initialized");

		this.isInitDone = true;

		this.sounds = new HashMap<>();
		this.playlist = new ArrayList<>();
		this.currentlyPlayingSounds = new HashSet<>();

		this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNamePlayList());
		this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());

		SafeAsyncTask task = new LoadSoundsFromDatabaseTask(this.dbSounds, this);
		task.execute();

		task = new LoadPlaylistFromDatabaseTask(this.dbPlaylist, this);
		task.execute();
	}

	@Override
	public boolean isInit()
	{
		return this.isInitDone;
	}

	@Override
	public void release()
	{
		this.isInitDone = false;

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

		this.eventBus.post(new PlaylistChangedEvent());
		this.eventBus.post(new SoundsRemovedEvent(null));
	}

	@Override
	public boolean isPlaylistPlayer(MediaPlayerData playerData)
	{
		return Playlist.TAG.equals(playerData.getFragmentTag());
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
	public void createSoundAndAddToManager(MediaPlayerData data)
	{
		if (this.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = this.createSound(data);
		if (player == null)
		{
			this.removeSoundDataFromDatabase(data);
			EventBus.getDefault().post(new CreatingPlayerFailedEvent(data));
		}
		else
			this.addSoundToSounds(player);
	}

	@Override
	public void createPlaylistSoundAndAddToManager(MediaPlayerData data)
	{
		if (this.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = this.createPlaylistSound(data);
		if (player == null)
		{
			this.removePlaylistDataFromDatabase(data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
		}
		else
			this.addSoundToPlayList(player);
	}

	@Override
	public void toggleSoundInPlaylist(String playerId, boolean addToPlaylist)
	{
		EnhancedMediaPlayer player = SoundsManagerUtil.searchInMapForId(playerId, this.sounds);
		EnhancedMediaPlayer playerInPlaylist = SoundsManagerUtil.searchInListForId(playerId, playlist);

		if (addToPlaylist)
		{
			if (playerInPlaylist != null)
				return;

			if (player != null)
			{
				player.setIsInPlaylist(true);

				EnhancedMediaPlayer playerForPlaylist = createPlaylistSound(player.getMediaPlayerData());
				if (playerForPlaylist == null)
				{
					this.removePlaylistDataFromDatabase(player.getMediaPlayerData());
					this.eventBus.post(new CreatingPlayerFailedEvent(player.getMediaPlayerData()));
				}
				else
					this.addSoundToPlayList(playerForPlaylist);
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

	private void addSoundToPlayList(EnhancedMediaPlayer player)
	{
		this.playlist.add(player);
		MediaPlayerData data = player.getMediaPlayerData();
		MediaPlayerDataDao dao = this.getDbPlaylist().getMediaPlayerDataDao();
		if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.getPlayerId())).list().size() == 0)
		{
			dao.insert(data);
		}

		this.eventBus.post(new PlaylistChangedEvent());
	}

	private void addSoundToSounds(EnhancedMediaPlayer player)
	{
		if (player == null)
			throw new NullPointerException("cannot add new Player, player is null");

		MediaPlayerData data = player.getMediaPlayerData();
		String fragmentTag = data.getFragmentTag();
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);
		soundsInFragment.add(player);

		MediaPlayerDataDao dao = this.getDbSounds().getMediaPlayerDataDao();
		if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.getPlayerId())).list().size() == 0)
			dao.insert(data);

		this.eventBus.post(new SoundAddedEvent(player));
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
				if (correspondingPlayerInPlaylist != null)
				{
					this.playlist.remove(correspondingPlayerInPlaylist);

					this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), correspondingPlayerInPlaylist.getMediaPlayerData());
					correspondingPlayerInPlaylist.destroy(true);
				}
			}
			this.removeSoundFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerToRemove.getMediaPlayerData());
			playerToRemove.destroy(true);
		}

		this.eventBus.post(new SoundsRemovedEvent(soundsToRemove));
	}

	@Override
	public void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove)
	{
		for (EnhancedMediaPlayer player : soundsToRemove)
			this.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), false);

		this.eventBus.post(new SoundsRemovedEvent(soundsToRemove));
	}

	@Override
	public void moveSoundInFragment(String fragmentTag, int from, int to)
	{
		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);

		EnhancedMediaPlayer playerToMove = soundsInFragment.remove(from);
		soundsInFragment.add(to, playerToMove);

		this.eventBus.post(new SoundMovedEvent(playerToMove, from, to));
	}

	private EnhancedMediaPlayer createPlaylistSound(MediaPlayerData playerData)
	{
		try
		{
			return EnhancedMediaPlayer.getInstanceForPlayList(playerData);
		}
		catch (IOException e)
		{
			Logger.d(TAG, playerData.toString() + " " + e.getMessage());
			this.removePlaylistDataFromDatabase(playerData);
			return null;
		}
	}

	private EnhancedMediaPlayer createSound(MediaPlayerData playerData)
	{
		try
		{
			return new EnhancedMediaPlayer(playerData, this);
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			this.removeSoundDataFromDatabase(playerData);
			return null;
		}
	}

	@Override
	public void removeSoundDataFromDatabase(MediaPlayerData playerData)
	{
		this.removeSoundFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerData);
	}

	@Override
	public void removePlaylistDataFromDatabase(MediaPlayerData playerData)
	{
		this.removeSoundFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerData);
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
