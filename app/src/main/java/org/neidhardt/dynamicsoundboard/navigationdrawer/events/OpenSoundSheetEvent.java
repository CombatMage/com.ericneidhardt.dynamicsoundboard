package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * Created by eric.neidhardt on 24.05.2015.
 */
public class OpenSoundSheetEvent
{
	private final SoundSheet soundSheet;

	public OpenSoundSheetEvent(SoundSheet soundSheet)
	{
		this.soundSheet = soundSheet;
	}

	public SoundSheet getSoundSheet()
	{
		return soundSheet;
	}
}
