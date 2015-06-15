package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class RequestToggleSoundInPlaylistEvent
{
	private String playerId;
	private boolean addToPlayList;

	public RequestToggleSoundInPlaylistEvent(String playerId, boolean addToPlayList)
	{
		this.playerId = playerId;
		this.addToPlayList = addToPlayList;
	}

	public String getPlayerId()
	{
		return playerId;
	}

	public boolean isAddToPlayList()
	{
		return addToPlayList;
	}
}
