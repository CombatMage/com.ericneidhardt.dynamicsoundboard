package org.neidhardt.dynamicsoundboard.mediaplayer.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * Created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerStateChangedEvent
{
	private final EnhancedMediaPlayer player;
	private final boolean isAlive;

	public MediaPlayerStateChangedEvent(EnhancedMediaPlayer player, boolean isAlive)
	{
		this.isAlive = isAlive;
		this.player = player;
	}

	public boolean isAlive()
	{
		return this.isAlive;
	}

	public String getPlayerId()
	{
		return this.player.getMediaPlayerData().getPlayerId();
	}

	public String getFragmentTag()
	{
		return this.player.getMediaPlayerData().getFragmentTag();
	}

	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}

	@Override
	public String toString() {
		return "MediaPlayerStateChangedEvent{" +
				"player=" + player +
				", isAlive=" + isAlive +
				'}';
	}
}
