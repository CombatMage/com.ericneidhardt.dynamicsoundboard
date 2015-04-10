package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */
public class SoundsLoadedEvent
{
	private MediaPlayerData loadedSoundData;

	public SoundsLoadedEvent(MediaPlayerData loadedSoundData)
	{
		this.loadedSoundData = loadedSoundData;
	}

	public MediaPlayerData getLoadedSoundData()
	{
		return loadedSoundData;
	}
}
