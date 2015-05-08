package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.events.SoundSheetsLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 08.05.2015.
 */
public class LoadSoundSheetsTask extends LoadTask<SoundSheet>
{
	private static final String TAG = LoadSoundSheetsTask.class.getName();

	private DaoSession daoSession;

	public LoadSoundSheetsTask(DaoSession daoSession)
	{
		this.daoSession = daoSession;
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
		EventBus.getDefault().postSticky(new SoundSheetsLoadedEvent(loadedSoundSheets));
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
