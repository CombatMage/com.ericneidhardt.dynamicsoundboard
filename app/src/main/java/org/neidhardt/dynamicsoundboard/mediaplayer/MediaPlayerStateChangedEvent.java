package org.neidhardt.dynamicsoundboard.mediaplayer;

/**
 * Created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerStateChangedEvent
{
	private final boolean isAlive;
	private final String playerId;
	private final String fragmentTag;

	public MediaPlayerStateChangedEvent(boolean isAlive, String playerId, String fragmentTag)
	{
		this.isAlive = isAlive;
		this.playerId = playerId;
		this.fragmentTag = fragmentTag;
	}

	public boolean isAlive()
	{
		return this.isAlive;
	}

	public String getPlayerId()
	{
		return this.playerId;
	}

	public String getFragmentTag()
	{
		return this.fragmentTag;
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
