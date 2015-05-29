package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * Created by eric.neidhardt on 28.05.2015.
 */
public interface OnOpenSoundSheetEventListener
{
	/**
	 * This is called by greenRobot eventBus in case the delivered SoundSheet should be opened by the application.
	 * @param event delivered OpenSoundSheetEvent
	 */
	void onEvent(OpenSoundSheetEvent event);
}
