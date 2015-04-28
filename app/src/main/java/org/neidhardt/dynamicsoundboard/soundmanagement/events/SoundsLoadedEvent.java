package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */
public class SoundsLoadedEvent
{
	private MediaPlayerData loadedSoundData;
	private boolean loadFromDatabase = true;

	public SoundsLoadedEvent(MediaPlayerData loadedSoundData)
	{
		this.loadedSoundData = loadedSoundData;
	}

	public MediaPlayerData getLoadedSoundData()
	{
		return loadedSoundData;
	}

	public boolean isLoadFromDatabase()
	{
		return loadFromDatabase;
	}
}
