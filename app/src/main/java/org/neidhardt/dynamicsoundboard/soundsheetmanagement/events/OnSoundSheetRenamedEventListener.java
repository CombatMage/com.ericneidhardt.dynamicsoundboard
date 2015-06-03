package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events;

/**
 * Created by eric.neidhardt on 03.06.2015.
 */
public interface OnSoundSheetRenamedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case the label of the current SoundSheet was edited.
	 * @param event delivered SoundSheetRenamedEvent
	 */
	void onEvent(SoundSheetRenamedEvent event);
}
