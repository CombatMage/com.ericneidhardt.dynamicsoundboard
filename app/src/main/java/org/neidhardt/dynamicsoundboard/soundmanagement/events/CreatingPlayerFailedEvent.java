package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class CreatingPlayerFailedEvent
{
	private MediaPlayerData failingPlayerData;

	public CreatingPlayerFailedEvent(MediaPlayerData failingPlayerData)
	{
		this.failingPlayerData = failingPlayerData;
	}

	public MediaPlayerData getFailingPlayerData()
	{
		return failingPlayerData;
	}
}
