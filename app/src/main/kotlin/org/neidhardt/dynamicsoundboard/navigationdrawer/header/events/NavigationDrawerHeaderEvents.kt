package org.neidhardt.dynamicsoundboard.navigationdrawer.header.events

/**
 * File created by eric.neidhardt on 11.11.2015.
 */
class OpenSoundLayoutsRequestedEvent

interface OnOpenSoundLayoutsEventListener
{
	/**
	 * This is called by greenRobot EventBus when the user clicks on the open SoundLayouts button in navigation drawer header.
	 * @param event delivered OpenSoundLayoutsEvent
	 */
	fun onEvent(event: OpenSoundLayoutsRequestedEvent)
}