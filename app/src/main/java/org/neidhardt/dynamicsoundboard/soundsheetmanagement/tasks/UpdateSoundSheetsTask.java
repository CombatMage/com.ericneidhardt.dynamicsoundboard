package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import roboguice.util.SafeAsyncTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 08.05.2015.
 */
public class UpdateSoundSheetsTask extends SafeAsyncTask<Void>
{
	private static final String TAG = UpdateSoundSheetsTask.class.getName();

	private final DaoSession daoSession;
	private final List<SoundSheet> soundSheets;

	public UpdateSoundSheetsTask(DaoSession daoSession, List<SoundSheet> soundSheets)
	{
		this.daoSession = daoSession;
		this.soundSheets = soundSheets;
	}

	@Override
	public Void call() throws Exception
	{
		this.daoSession.getSoundSheetDao().deleteAll();
		this.daoSession.getSoundSheetDao().insertInTx(soundSheets);
		return null;
	}

	@Override
	protected void onException(Exception e) throws RuntimeException
	{
		super.onException(e);
		Logger.e(TAG, e.getMessage());
		throw new RuntimeException(e);
	}
}
