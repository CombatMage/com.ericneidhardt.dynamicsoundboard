package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class RequestMoveSoundEvent
{
	private String playerId;
	private int from;
	private int to;

	public RequestMoveSoundEvent(String playerId, int from, int to)
	{
		this.playerId = playerId;
		this.from = from;
		this.to = to;
	}

	public String getPlayerId()
	{
		return playerId;
	}

	public int getFrom()
	{
		return from;
	}

	public int getTo()
	{
		return to;
	}

	@Override
	public String toString()
	{
		return "RequestMoveSoundEvent{" +
				"playerId='" + playerId + '\'' +
				", from=" + from +
				", to=" + to +
				'}';
	}
}
