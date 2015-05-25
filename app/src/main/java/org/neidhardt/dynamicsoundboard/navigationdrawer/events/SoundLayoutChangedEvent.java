package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 25.05.2015.
 */
public class SoundLayoutChangedEvent
{
	private final SoundLayout newSoundLayout;

	public SoundLayoutChangedEvent(SoundLayout newSoundLayout)
	{
		this.newSoundLayout = newSoundLayout;
	}

	public SoundLayout getNewSoundLayout()
	{
		return newSoundLayout;
	}

	@Override
	public String toString()
	{
		return "SoundLayoutChangedEvent{" +
				"newSoundLayout=" + newSoundLayout +
				'}';
	}
}
