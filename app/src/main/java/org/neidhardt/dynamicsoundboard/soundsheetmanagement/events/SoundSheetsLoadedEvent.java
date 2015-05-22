package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * Created by eric.neidhardt on 27.03.2015.
 */
public class SoundSheetsLoadedEvent
{
	private List<SoundSheet> loadedSoundSheets;

	public SoundSheetsLoadedEvent(List<SoundSheet> loadedSoundSheets)
	{
		this.loadedSoundSheets = loadedSoundSheets;
	}

	public List<SoundSheet> getLoadedSoundSheets()
	{
		return loadedSoundSheets;
	}
}
