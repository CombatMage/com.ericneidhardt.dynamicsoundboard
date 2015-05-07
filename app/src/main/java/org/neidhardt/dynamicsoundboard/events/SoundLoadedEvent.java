package org.neidhardt.dynamicsoundboard.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */
public class SoundLoadedEvent
{
	private MediaPlayerData loadedSoundData;
	private boolean loadFromDatabase = true;

	public SoundLoadedEvent(MediaPlayerData loadedSoundData, boolean loadFromDatabase)
	{
		this.loadedSoundData = loadedSoundData;
		this.loadFromDatabase = loadFromDatabase;
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
