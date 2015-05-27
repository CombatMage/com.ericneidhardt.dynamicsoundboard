package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public class OpenSoundLayoutSettingsEvent
{
	private final SoundLayout soundLayout;

	public OpenSoundLayoutSettingsEvent(SoundLayout soundLayout)
	{
		this.soundLayout = soundLayout;
	}

	public SoundLayout getSoundLayout()
	{
		return soundLayout;
	}
}
