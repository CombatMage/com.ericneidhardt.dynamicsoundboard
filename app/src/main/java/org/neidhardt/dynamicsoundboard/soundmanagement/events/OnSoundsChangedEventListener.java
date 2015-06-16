package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public interface OnSoundsChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the loaded sounds have changed, either in number or data.
	 * @param event delivered SoundsChangedEvent
	 */
	void onEvent(SoundsChangedEvent event);
}
