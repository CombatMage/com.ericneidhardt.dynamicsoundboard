package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 25.05.2015.
 */
public class SoundLayoutChangedEvent
{
	public enum REQUEST
	{
		CURRENT_LAYOUT_CHANGED,
		LAYOUT_LIST_CHANGE,
	}

	private final SoundLayout newSoundLayout;
	private final REQUEST requestedAction;

	public SoundLayoutChangedEvent(SoundLayout newSoundLayout, REQUEST requestedAction)
	{
		this.newSoundLayout = newSoundLayout;
		this.requestedAction = requestedAction;
	}

	public SoundLayout getNewSoundLayout()
	{
		return newSoundLayout;
	}

	public REQUEST getRequestedAction()
	{
		return requestedAction;
	}

	@Override
	public String toString()
	{
		return "SoundLayoutChangedEvent{" +
				"newSoundLayout=" + newSoundLayout +
				'}';
	}
}
