package org.neidhardt.dynamicsoundboard.soundactivity.events;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public interface ActivityStateChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the activity state has changed, ie. the onResume() or onPause() callback
	 * of the {@code SoundActivity} is called.
	 * @param event delivered ActivityStateChangedEvent
	 */
	void onEvent(ActivityStateChangedEvent event);
}
