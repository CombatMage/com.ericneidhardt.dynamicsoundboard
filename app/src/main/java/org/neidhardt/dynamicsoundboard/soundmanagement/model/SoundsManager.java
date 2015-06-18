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
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundAddedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.UpdateSoundsTask;
import roboguice.util.SafeAsyncTask;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.deepToString;

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
			throw new IllegalStateException(TAG + ": init() was called, but SoundsManager was already initialized");

		this.isInitDone = true;

		this.sounds = new HashMap<>();
		this.playlist = new ArrayList<>();
		this.currentlyPlayingSounds = new HashSet<>();

		this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNamePlayList());
		this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), SoundsManagerUtil.getDatabaseNameSounds());

		SafeAsyncTask task = new LoadSoundsTask(this.dbSounds, this);
		task.execute();

		task = new LoadPlaylistTask(this.dbPlaylist, this);
		task.execute();
	}

	@Override
	public boolean isInit()
	{
		return this.isInitDone;
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

	@Override
	public void addSoundToPlayList(EnhancedMediaPlayer player)
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

	@Override
	public void addSoundToSounds(EnhancedMediaPlayer player)
	{
		if (player == null)
			throw new NullPointerException("cannot add new Player, player is null");

		MediaPlayerData data = player.getMediaPlayerData();
		String fragmentTag = data.getFragmentTag();
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		Integer index = data.getSortOrder();
		if (index == null)
			index = 0;

		List<EnhancedMediaPlayer> soundsInFragment = this.sounds.get(fragmentTag);
		int count = soundsInFragment.size();
		if (index <= count) // add item according to sort order
			soundsInFragment.add(index, player);
		else
			soundsInFragment.add(player); // if the list is to short, just append

		MediaPlayerDataDao dao = this.getDbSounds().getMediaPlayerDataDao();
		if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.getPlayerId())).list().size() == 0)
		{
			dao.insert(data);
		}

		this.eventBus.post(new SoundAddedEvent(player));
	}

	@Override
	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		if (soundsToRemove == null || soundsToRemove.size() == 0)
			return;

		Set<String> affectedSoundSheets = new HashSet<>(); // we need to cleanup the sort order of all affected sound lists.

		List<EnhancedMediaPlayer> copyList = new ArrayList<>(soundsToRemove.size());
		copyList.addAll(soundsToRemove); // this is done to prevent concurrent modification exception

		for (EnhancedMediaPlayer playerToRemove : copyList)
		{
			MediaPlayerData data = playerToRemove.getMediaPlayerData();
			affectedSoundSheets.add(data.getFragmentTag());
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

		for (String fragmentTag : affectedSoundSheets)
			this.cleanupSortOrderInList(this.getSoundsInFragment(fragmentTag));

		this.eventBus.post(new SoundsRemovedEvent(soundsToRemove));
	}

	private void cleanupSortOrderInList(List<EnhancedMediaPlayer> sounds)
	{
		int count = sounds.size();
		for (int i = 0; i < count; i++)
		{
			sounds.get(i).getMediaPlayerData().setSortOrder(count);
			sounds.get(i).getMediaPlayerData().setItemWasUpdated();
		}
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

		int count = soundsInFragment.size();
		int indexOfSoundsToUpdate = Math.min(from, to); // we need to update all sound after the moved one
		for (int i = indexOfSoundsToUpdate; i < count; i++)
		{
			soundsInFragment.get(i).getMediaPlayerData().setSortOrder(i);
			soundsInFragment.get(i).getMediaPlayerData().setItemWasAltered();
		}
	}

	@Override
	public EnhancedMediaPlayer createPlaylistSound(MediaPlayerData playerData)
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

	@Override
	public EnhancedMediaPlayer createSound(MediaPlayerData playerData)
	{
		int itemsInFragment = this.sounds.get(playerData.getFragmentTag()) != null ? this.sounds.get(playerData.getFragmentTag()).size() : 0;
		if (playerData.getSortOrder() == null || playerData.getSortOrder() > itemsInFragment)
			playerData.setSortOrder(itemsInFragment);
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
