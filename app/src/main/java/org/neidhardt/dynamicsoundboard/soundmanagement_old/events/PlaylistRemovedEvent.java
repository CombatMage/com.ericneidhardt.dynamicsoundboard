package org.neidhardt.dynamicsoundboard.soundmanagement_old.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * File created by eric.neidhardt on 25.05.2015.
 */
public class PlaylistRemovedEvent
{
	private final List<EnhancedMediaPlayer> playersToRemove;

	public PlaylistRemovedEvent(List<EnhancedMediaPlayer> playersToRemove)
	{
		this.playersToRemove = playersToRemove;
	}

	@Override
	public String toString()
	{
		return "PlaylistRemovedEvent{" +
				"playersToRemove=" + playersToRemove +
				'}';
	}
}
