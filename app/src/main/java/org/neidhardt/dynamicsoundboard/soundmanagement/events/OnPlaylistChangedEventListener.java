package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * Created by eric.neidhardt on 29.05.2015.
 */
public interface OnPlaylistChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the playlist has changed.*
	 * @param event delivered PlaylistRemovedEvent
	 */
	void onEventMainThread(PlaylistChangedEvent event);
}
