package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * Created by eric.neidhardt on 25.05.2015.
 */
public class PlaylistSoundsRemovedEvent
{
	private final List<EnhancedMediaPlayer> playersToRemove;

	public PlaylistSoundsRemovedEvent(List<EnhancedMediaPlayer> playersToRemove)
	{
		this.playersToRemove = playersToRemove;
	}

	public List<EnhancedMediaPlayer> getPlayersToRemove()
	{
		return playersToRemove;
	}

	@Override
	public String toString()
	{
		return "PlaylistSoundsRemovedEvent{" +
				"playersToRemove=" + playersToRemove +
				'}';
	}
}
