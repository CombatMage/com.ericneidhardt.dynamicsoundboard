package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * Created by eric.neidhardt on 03.06.2015.
 */
public class SoundSheetRenamedEvent
{
	private final String fragmentTag;
	private final String newLabel;

	public SoundSheetRenamedEvent(String fragmentTag, String newLabel)
	{
		this.fragmentTag = fragmentTag;
		this.newLabel = newLabel;
	}

	public String getNewLabel()
	{
		return newLabel;
	}

	public String getFragmentTag()
	{
		return fragmentTag;
	}
}
