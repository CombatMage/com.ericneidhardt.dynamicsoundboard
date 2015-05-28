package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * Created by eric.neidhardt on 28.05.2015.
 */
public class SoundSheetsFromFileLoadedEvent
{
	private List<SoundSheet> soundSheetList;

	public SoundSheetsFromFileLoadedEvent(List<SoundSheet> soundSheetList)
	{
		this.soundSheetList = soundSheetList;
	}

	public List<SoundSheet> getSoundSheetList()
	{
		return soundSheetList;
	}
}
