package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public class SoundLayoutAddedEvent
{
	private final SoundLayout data;

	public SoundLayoutAddedEvent(SoundLayout data)
	{
		this.data = data;
	}

	public SoundLayout getData()
	{
		return data;
	}
}
