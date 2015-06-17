package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 17.06.2015.
 */
public class SoundChangedEvent
{
	private EnhancedMediaPlayer player;

	public SoundChangedEvent(EnhancedMediaPlayer player)
	{
		this.player = player;
	}

	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}
}
