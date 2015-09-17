package org.neidhardt.dynamicsoundboard.soundmanagement.events

/**
 * File created by eric.neidhardt on 29.05.2015.
 */
public interface OnPlaylistChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the playlist has changed.*
	 * @param event delivered PlaylistChangedEvent
	 */
	@SuppressWarnings("unused")
	public fun onEventMainThread(event: PlaylistChangedEvent)
}
