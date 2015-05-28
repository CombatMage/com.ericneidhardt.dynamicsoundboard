package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

/**
 * Created by eric.neidhardt on 28.05.2015.
 */
public class OpenSoundSheetEvent
{
	private final SoundSheet soundSheetToOpen;

	public OpenSoundSheetEvent(SoundSheet soundSheetToOpen)
	{
		this.soundSheetToOpen = soundSheetToOpen;
	}

	public SoundSheet getSoundSheetToOpen()
	{
		return soundSheetToOpen;
	}
}
