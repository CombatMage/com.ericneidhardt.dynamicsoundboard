package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events

/**
 * File created by eric.neidhardt on 20.07.2015.
 */
interface OnOpenSoundLayoutSettingsEventListener
{
	/**
	 * This is called by greenRobot EventBus when the settingsButton dialog for a certain SoundLayout is requested.
	 * @param event Delivered OpenSoundLayoutSettingsEvent
	 */
	fun onEvent(event: OpenSoundLayoutSettingsEvent)
}