package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * File created by eric.neidhardt on 28.05.2015.
 */
public interface OnSoundSheetsChangedEventListener
{
	/**
	 * This is called by greenRobot eventBus in case the list of SoundSheets has been changed.
	 * @param event delivered SoundSheetsChangedEvent
	 */
	void onEvent(SoundSheetsChangedEvent event);
}
