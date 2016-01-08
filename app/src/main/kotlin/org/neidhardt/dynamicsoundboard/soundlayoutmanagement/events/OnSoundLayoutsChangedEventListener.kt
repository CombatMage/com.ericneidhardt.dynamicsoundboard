package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events

/**
 * File created by eric.neidhardt on 08.01.2016.
 */
public interface OnSoundLayoutsChangedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case one or more {@code SoundLayout}s have been removed.
	 * @param event delivered SoundLayoutsRemovedEvent
	 */
	@SuppressWarnings("unused")
	public fun onEventMainThread(event: SoundLayoutsRemovedEvent)
}

public interface OnSoundLayoutSelectedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a new SoundLayout was selected.
	 * @param event delivered SoundLayoutRenamedEvent
	 */
	public fun onEvent(event: SoundLayoutSelectedEvent)
}