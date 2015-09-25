package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

/**
 * File created by eric.neidhardt on 08.06.2015.
 */
public interface OnActionModeChangeRequestedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a request to change the current contextual action mode has benn submitted.
	 * @param event delivered OpenSoundSheetEvent
	 */
	void onEvent(ActionModeChangeRequestedEvent event);
}
