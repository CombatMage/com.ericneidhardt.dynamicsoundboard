package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

import java.util.List;

/**
 * File created by eric.neidhardt on 08.05.2015.
 */
public class LoadSoundSheetsTask extends LongTermTask<List<SoundSheet>>
{
	private static final String TAG = LoadSoundSheetsTask.class.getName();

	private DaoSession daoSession;
	private SoundSheetsDataStorage soundSheetsDataStorage;

	public LoadSoundSheetsTask(DaoSession daoSession, SoundSheetsDataStorage soundSheetsDataStorage)
	{
		this.daoSession = daoSession;
		this.soundSheetsDataStorage = soundSheetsDataStorage;
	}

	@Override
	public List<SoundSheet> call() throws Exception
	{
		return this.daoSession.getSoundSheetDao().queryBuilder().list();
	}

	@Override
	protected void onSuccess(List<SoundSheet> loadedSoundSheets) throws Exception
	{
		super.onSuccess(loadedSoundSheets);
		this.soundSheetsDataStorage.addLoadedSoundSheets(loadedSoundSheets);
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
