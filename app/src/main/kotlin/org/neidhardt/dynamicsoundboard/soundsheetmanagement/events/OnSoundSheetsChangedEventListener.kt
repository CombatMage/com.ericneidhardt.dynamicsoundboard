package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
interface OnSoundSheetsChangedEventListener
{
	/**
	 * This is called by greenRobot eventBus in case a new {@code SoundSheet} was added.
	 * @param event delivered SoundSheetAddedEvent
	 */
	fun onEvent(event: SoundSheetAddedEvent) {}

	/**
	 * This is called by greenRobot eventBus in case the list of SoundSheets has been changed.
	 * @param event delivered SoundSheetsChangedEvent
	 */
	fun onEvent(event: SoundSheetChangedEvent) {}

	/**
	 * This is called by greenRobot EventBus in case one or more {@code SoundSheet}s have been removed.
	 * @param event delivered SoundSheetsRemovedEvent
	 */
	fun onEvent(event: SoundSheetsRemovedEvent) {}
}