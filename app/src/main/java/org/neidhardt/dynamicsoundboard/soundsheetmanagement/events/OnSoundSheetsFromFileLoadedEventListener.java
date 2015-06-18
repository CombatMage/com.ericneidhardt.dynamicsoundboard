package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * File created by eric.neidhardt on 03.06.2015.
 */
public interface OnSoundSheetsFromFileLoadedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case SoundSheets fromm file were loaded.
	 * @param event delivered SoundSheetsFromFileLoadedEvent
	 */
	void onEvent(SoundSheetsFromFileLoadedEvent event);
}
