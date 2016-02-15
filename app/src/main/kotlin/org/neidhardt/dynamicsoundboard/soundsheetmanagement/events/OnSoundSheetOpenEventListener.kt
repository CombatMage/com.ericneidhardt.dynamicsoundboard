package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

/**
 * File created by eric.neidhardt on 28.05.2015.
 */
interface OnSoundSheetOpenEventListener
{
	/**
	 * This is called by greenRobot eventBus in case the delivered SoundSheet should be opened by the application.
	 * @param event delivered OpenSoundSheetEvent
	 */
	fun onEvent(event: OpenSoundSheetEvent)
}

interface OnSoundSheetsInitEventLisenter
{
	fun onEvent(event: SoundSheetsInitEvent)
}