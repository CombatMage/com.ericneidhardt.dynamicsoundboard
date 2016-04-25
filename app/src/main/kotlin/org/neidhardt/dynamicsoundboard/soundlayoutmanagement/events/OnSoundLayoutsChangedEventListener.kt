package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events

/**
 * File created by eric.neidhardt on 08.01.2016.
 */
interface OnSoundLayoutsChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case one or more {@code SoundLayout}s have been removed.
	 * @param event delivered SoundLayoutsRemovedEvent
	 */
	fun onEvent(event: SoundLayoutsRemovedEvent)
}

interface OnSoundLayoutSelectedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a new SoundLayout was selected.
	 * @param event delivered SoundLayoutRenamedEvent
	 */
	fun onEvent(event: SoundLayoutSelectedEvent)
}