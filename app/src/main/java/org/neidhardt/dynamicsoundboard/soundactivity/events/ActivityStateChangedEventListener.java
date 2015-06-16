package org.neidhardt.dynamicsoundboard.soundactivity.events;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public interface ActivityStateChangedEventListener
{
	void onEvent(ActivityClosedEvent event);

	void onEvent(ActivityResumedEvent event);
}
