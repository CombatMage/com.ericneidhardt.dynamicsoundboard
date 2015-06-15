package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*;

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
	EventBus eventBus;

	private Map<String, List<EnhancedMediaPlayer>> sounds;
	private List<EnhancedMediaPlayer> playlist;
	private Set<EnhancedMediaPlayer> currentlyPlayingSounds;

	public SoundsManager()
	{
		this.eventBus = EventBus.getDefault();
	}

	@Override
	public void init()
	{
		this.sounds = new HashMap<>();
		this.playlist = new ArrayList<>();
		this.currentlyPlayingSounds = new HashSet<>();

		this.eventBus.post(new RequestInitEvent());
	}

	@Override
	public void writeCacheBack()
	{
		this.eventBus.post(new RequestWriteCachBackEvent());
	}

	@Override
	public void registerOnEventBus()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this);
	}

	@Override
	public void unregisterOnEventBus()
	{
		this.eventBus.unregister(this);
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
			soundsInFragment = new ArrayList<EnhancedMediaPlayer>();
			this.sounds.put(fragmentTag, soundsInFragment);
		}
		return soundsInFragment;
	}

	@Override
	public EnhancedMediaPlayer getSoundById(String fragmentTag, String playerId)
	{
		if (fragmentTag.equals(Playlist.TAG))
			return this.searchInListForId(playerId, playlist);
		else
			return this.searchInListForId(playerId, this.sounds.get(fragmentTag));
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

	@Override
	public void toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		this.eventBus.post(new RequestToggleSoundInPlaylistEvent(playerId, addToPlayList));
	}

	@Override
	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		this.eventBus.post(new RequestRemoveSoundsEvent(soundsToRemove, false));
	}

	@Override
	public void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove)
	{
		this.eventBus.post(new RequestRemoveSoundsEvent(soundsToRemove, true));
	}

	@Override
	public void moveSoundInFragment(String fragmentTag, int from, int to)
	{
		this.eventBus.post(new RequestMoveSoundEvent(fragmentTag, from, to));
	}

}
