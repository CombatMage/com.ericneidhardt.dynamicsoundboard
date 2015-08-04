package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events

/**
 * File created by eric.neidhardt on 20.07.2015.
 */
public interface OnSoundLayoutRemovedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a new SoundLayout was renamed.
	 * @param event delivered SoundLayoutRenamedEvent
	 */
	public fun onEvent(event: SoundLayoutRemovedEvent)
}

public interface OnSoundLayoutSelectedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a new SoundLayout was selected.
	 * @param event delivered SoundLayoutRenamedEvent
	 */
	public fun onEvent(event: SoundLayoutSelectedEvent)
}

public interface OnOpenSoundLayoutSettingsEvent
{
	/**
	 * This is called by greenRobot EventBus when the settings dialog for a certain SoundLayout is requested.
	 * @param event Delivered OpenSoundLayoutSettingsEvent
	 */
	public fun onEvent(event: OpenSoundLayoutSettingsEvent)
}