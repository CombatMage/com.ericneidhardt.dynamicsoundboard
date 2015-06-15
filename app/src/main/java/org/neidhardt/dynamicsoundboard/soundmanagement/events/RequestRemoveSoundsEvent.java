package org.neidhardt.dynamicsoundboard.soundmanagement.events;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class RequestRemoveSoundsEvent
{
	private List<EnhancedMediaPlayer> soundsToRemove;
	private boolean removeFromPlaylist;

	public RequestRemoveSoundsEvent(List<EnhancedMediaPlayer> soundsToRemove, boolean removeFromPlaylist)
	{
		this.soundsToRemove = soundsToRemove;
		this.removeFromPlaylist = removeFromPlaylist;
	}

	public List<EnhancedMediaPlayer> getSoundsToRemove()
	{
		return soundsToRemove;
	}

	public boolean isRemoveFromPlaylist()
	{
		return removeFromPlaylist;
	}
}
