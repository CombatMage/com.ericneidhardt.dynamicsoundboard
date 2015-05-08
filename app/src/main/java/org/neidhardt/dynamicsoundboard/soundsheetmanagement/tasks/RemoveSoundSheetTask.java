package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

/**
 * Created by eric.neidhardt on 08.05.2015.
 */
public class RemoveSoundSheetTask extends SafeAsyncTask<Void>
{
	private static final String TAG = RemoveSoundSheetTask.class.getName();

	private SoundSheet soundSheet;
	private DaoSession daoSession;

	public RemoveSoundSheetTask(DaoSession daoSession, SoundSheet soundSheet)
	{
		this.soundSheet = soundSheet;
		this.daoSession = daoSession;
	}

	@Override
	public Void call() throws Exception
	{
		daoSession.getSoundSheetDao().delete(this.soundSheet);
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
