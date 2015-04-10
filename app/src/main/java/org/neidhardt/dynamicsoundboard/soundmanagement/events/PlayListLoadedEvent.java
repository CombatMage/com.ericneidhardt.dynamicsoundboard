package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */
public class PlayListLoadedEvent
{
	private MediaPlayerData loadedSoundData;

	public PlayListLoadedEvent(MediaPlayerData loadedSoundData)
	{
		this.loadedSoundData = loadedSoundData;
	}

	public MediaPlayerData getLoadedSoundData()
	{
		return loadedSoundData;
	}
}
