package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * File created by eric.neidhardt on 24.03.2015.
 */
public class SoundLoadedEvent
{
	private MediaPlayerData newSoundData;
	private boolean loadFromDatabase = true;

	public SoundLoadedEvent(MediaPlayerData newSoundData, boolean loadFromDatabase)
	{
		this.newSoundData = newSoundData;
		this.loadFromDatabase = loadFromDatabase;
	}

	public MediaPlayerData getNewSoundData()
	{
		return newSoundData;
	}

	public boolean isLoadFromDatabase()
	{
		return loadFromDatabase;
	}

	@Override
	public String toString()
	{
		return "SoundLoadedEvent{" +
				"newSoundData=" + newSoundData +
				", loadFromDatabase=" + loadFromDatabase +
				'}';
	}
}
