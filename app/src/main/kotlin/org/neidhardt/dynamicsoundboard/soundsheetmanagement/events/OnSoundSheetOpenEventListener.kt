package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

/**
 * File created by eric.neidhardt on 28.05.2015.
 */
public interface OnSoundSheetOpenEventListener
{
	/**
	 * This is called by greenRobot eventBus in case the delivered SoundSheet should be opened by the application.
	 * @param event delivered OpenSoundSheetEvent
	 */
	public fun onEvent(event: OpenSoundSheetEvent)
}

public interface OnSoundSheetsInitEventLisenter
{
	public fun onEvent(event: SoundSheetsInitEvent)
}