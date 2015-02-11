package org.neidhardt.dynamicsoundboard.mediaplayer;

/**
 * Created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerStateChangedEvent
{
	private final boolean isPlaying;
	private final boolean isFinished;
	private final boolean isAlive;
	private final String playerId;

	public MediaPlayerStateChangedEvent(boolean isPlaying, boolean isFinished, boolean isAlive, String playerId)
	{
		this.isPlaying = isPlaying;
		this.isFinished = isFinished;
		this.isAlive = isAlive;
		this.playerId = playerId;
	}

	public boolean isPlaying()
	{
		return this.isPlaying;
	}

	public boolean isFinished()
	{
		return this.isFinished;
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
				"isPlaying=" + isPlaying +
				", isFinished=" + isFinished +
				", isAlive=" + isAlive +
				", playerId='" + playerId + '\'' +
				'}';
	}
}
