package org.neidhardt.dynamicsoundboard.mediaplayer.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerCompletedEvent
{
	private EnhancedMediaPlayer player;

	public MediaPlayerCompletedEvent(EnhancedMediaPlayer player)
	{
		this.player = player;
	}

	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}
}
