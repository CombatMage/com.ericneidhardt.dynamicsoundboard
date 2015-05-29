package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * Created by eric.neidhardt on 29.05.2015.
 */
public interface OnSoundSheetsLoadedEventListener
{
	/**
	 * This is called by greenRobot EventBus when LoadSoundSheetsTask has been finished loading sound sheets.
	 * @param event delivered SoundSheetsLoadedEvent
	 */
	void onEventMainThread(SoundSheetsLoadedEvent event);
}
