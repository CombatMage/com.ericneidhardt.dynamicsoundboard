package org.neidhardt.dynamicsoundboard.soundactivity.events;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public interface OnSoundsLoadedEventListener
{
	/**
	 * This is called by greenRobot EventBus, in case a sounds have been loaded from database or file system and is ready to
	 * be added to the {@code SoundManager}.
	 * @param event delivered SoundLoadedEvent
	 */
	void onEventMainThread(SoundLoadedEvent event);

	/**
	 * This is called by greenRobot EventBus, in case the playlist has been loaded from database or file system and is ready to
	 * be added to the {@code SoundManager}.
	 * @param event delivered SoundLoadedEvent
	 */
	void onEventMainThread(PlaylistLoadedEvent event);
}
