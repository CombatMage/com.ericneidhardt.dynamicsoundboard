package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public class SoundAddedEvent
{
	private EnhancedMediaPlayer player;

	public SoundAddedEvent(EnhancedMediaPlayer player)
	{
		this.player = player;
	}

	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}
}
