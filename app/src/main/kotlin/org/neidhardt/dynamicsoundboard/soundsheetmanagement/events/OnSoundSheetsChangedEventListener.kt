package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
public interface OnSoundSheetsChangedEventListener
{
	/**
	 * This is called by greenRobot eventBus in case a new {@code SoundSheet} was added.
	 * @param event delivered SoundSheetAddedEvent
	 */
	SuppressWarnings("unused")
	public fun onEventMainThread(event: SoundSheetAddedEvent)

	/**
	 * This is called by greenRobot eventBus in case the list of SoundSheets has been changed.
	 * @param event delivered SoundSheetsChangedEvent
	 */
	SuppressWarnings("unused")
	public fun onEventMainThread(event: SoundSheetChangedEvent)

	/**
	 * This is called by greenRobot EventBus in case one or more {@code SoundSheet}s have been removed.
	 * @param event delivered SoundSheetsRemovedEvent
	 */
	SuppressWarnings("unused")
	public fun onEventMainThread(event: SoundSheetsRemovedEvent)
}