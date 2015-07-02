package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
public class SoundMovedEvent
{
	private EnhancedMediaPlayer player;
	private int from;
	private int to;

	public SoundMovedEvent(EnhancedMediaPlayer player, int from, int to)
	{
		this.player = player;
		this.from = from;
		this.to = to;
	}

	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}

	public int getFrom()
	{
		return from;
	}

	public int getTo()
	{
		return to;
	}
}
