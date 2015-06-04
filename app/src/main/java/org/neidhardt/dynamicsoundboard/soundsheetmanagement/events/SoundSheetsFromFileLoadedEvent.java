package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * File created by eric.neidhardt on 28.05.2015.
 */
public class SoundSheetsFromFileLoadedEvent
{
	private List<SoundSheet> newSoundSheetList;
	private List<SoundSheet> oldSoundSheetList;

	public SoundSheetsFromFileLoadedEvent(List<SoundSheet> newSoundSheetList, List<SoundSheet> oldSoundSheetList)
	{
		this.newSoundSheetList = newSoundSheetList;
		this.oldSoundSheetList = oldSoundSheetList;
	}

	public List<SoundSheet> getNewSoundSheetList()
	{
		return newSoundSheetList;
	}

	public List<SoundSheet> getOldSoundSheetList()
	{
		return oldSoundSheetList;
	}
}
