package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public interface OnSoundsChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a new sound was add.
	 * @param event delivered SoundAddedEvent
	 */
	void onEventMainThread(SoundAddedEvent event);

	/**
	 * This is called by greenRobot EventBus in case some sounds have been removed.
	 * @param event delivered SoundsRemovedEvent
	 */
	void onEventMainThread(SoundsRemovedEvent event);
}
