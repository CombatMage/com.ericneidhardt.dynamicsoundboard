package org.neidhardt.dynamicsoundboard.mediaplayer;

/**
 * Created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerStateChangedEvent
{
	private final boolean isAlive;
	private final String playerId;

	public MediaPlayerStateChangedEvent(boolean isAlive, String playerId)
	{
		this.isAlive = isAlive;
		this.playerId = playerId;
	}

	public boolean isAlive()
	{
		return this.isAlive;
	}

	public String getPlayerId()
	{
		return this.playerId;
	}

	@Override
	public String toString()
	{
		return "MediaPlayerStateChangedEvent{" +
				", isAlive=" + isAlive +
				", playerId='" + playerId + '\'' +
				'}';
	}
}
