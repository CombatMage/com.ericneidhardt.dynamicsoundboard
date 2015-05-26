package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 26.05.2015.
 */
public class SoundLayoutSelectedEvent
{
	private final SoundLayout selectedSoundLayout;

	public SoundLayoutSelectedEvent(SoundLayout selectedSoundLayout)
	{
		this.selectedSoundLayout = selectedSoundLayout;
	}

	@Override
	public String toString()
	{
		return "SoundLayoutSelectedEvent{" +
				"selectedSoundLayout=" + selectedSoundLayout +
				'}';
	}
}
