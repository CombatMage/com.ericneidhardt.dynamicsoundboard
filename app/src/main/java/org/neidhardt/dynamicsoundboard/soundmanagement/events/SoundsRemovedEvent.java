package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public class SoundsRemovedEvent
{
	private List<EnhancedMediaPlayer> players;

	public SoundsRemovedEvent(List<EnhancedMediaPlayer> players)
	{
		this.players = players;
	}

	public SoundsRemovedEvent()
	{
		this.players = null;
	}

	public List<EnhancedMediaPlayer> getPlayers()
	{
		return players;
	}

	public boolean removeAll()
	{
		return this.players == null;
	}
}
