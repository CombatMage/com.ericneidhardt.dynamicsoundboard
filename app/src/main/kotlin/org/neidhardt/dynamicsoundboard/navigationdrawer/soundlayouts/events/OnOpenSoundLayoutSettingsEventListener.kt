package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events

/**
 * File created by eric.neidhardt on 20.07.2015.
 */
public interface OnOpenSoundLayoutSettingsEventListener
{
	/**
	 * This is called by greenRobot EventBus when the settings dialog for a certain SoundLayout is requested.
	 * @param event Delivered OpenSoundLayoutSettingsEvent
	 */
	public fun onEvent(event: OpenSoundLayoutSettingsEvent)
}