package org.neidhardt.dynamicsoundboard.soundcontrol.events;

/**
 * Created by eric.neidhardt on 29.05.2015.
 */
public interface OnOpenSoundDialogEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the rename dialog for a certain sound is requested.
	 * @param event delivered OpenSoundRenameEvent
	 */
	void onEvent(OpenSoundRenameEvent event);

	/**
	 * This is called by greenRobot EventBus in case the settings for a certain sound are requested.
	 * @param event delivered OpenSoundSettingsEvent
	 */
	void onEvent(OpenSoundSettingsEvent event);
}
