package org.neidhardt.dynamicsoundboard.soundmanagement_old.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * File created by eric.neidhardt on 24.03.2015.
 */
public class AddNewSoundEvent
{
	private MediaPlayerData newSoundData;
	private boolean loadFromDatabase = true;

	public AddNewSoundEvent(MediaPlayerData newSoundData, boolean loadFromDatabase)
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
}
