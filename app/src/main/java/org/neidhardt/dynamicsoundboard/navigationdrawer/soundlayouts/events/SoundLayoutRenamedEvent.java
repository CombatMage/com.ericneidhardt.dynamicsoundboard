package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 25.05.2015.
 */
public class SoundLayoutRenamedEvent
{
	private final SoundLayout renamedSoundLayout;

	public SoundLayoutRenamedEvent(SoundLayout renamedSoundLayout)
	{
		this.renamedSoundLayout = renamedSoundLayout;
	}

	@Override
	public String toString()
	{
		return "SoundLayoutRenamedEvent{" +
				"renamedSoundLayout=" + renamedSoundLayout +
				'}';
	}
}
