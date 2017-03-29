package org.neidhardt.dynamicsoundboard.soundcontrol.events

/**
 * File created by eric.neidhardt on 29.05.2015.
 */
interface OnOpenSoundDialogEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the rename dialog for a certain sound is requested.
	 * @param event delivered OpenSoundRenameEvent
	 */
	fun onEvent(event: OpenSoundRenameEvent)

	/**
	 * This is called by greenRobot EventBus in case the settingsButton for a certain sound are requested.
	 * @param event delivered OpenSoundSettingsEvent
	 */
	fun onEvent(event: OpenSoundSettingsEvent)
}
