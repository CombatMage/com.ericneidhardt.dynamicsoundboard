package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * Created by eric.neidhardt on 23.05.2015.
 */
public class SoundSheetsRemovedEvent
{
	private final SoundSheet removedSoundSheet;

	public SoundSheetsRemovedEvent(SoundSheet removedSoundSheet)
	{
		this.removedSoundSheet = removedSoundSheet;
	}

	public SoundSheet getRemovedSoundSheet()
	{
		return removedSoundSheet;
	}
}
