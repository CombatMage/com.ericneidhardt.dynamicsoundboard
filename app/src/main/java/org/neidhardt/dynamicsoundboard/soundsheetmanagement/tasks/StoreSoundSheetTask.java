package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

/**
 * Created by eric.neidhardt on 08.05.2015.
 */
public class StoreSoundSheetTask extends SafeAsyncTask<Void>
{
	private final String TAG = StoreSoundSheetTask.class.getName();

	private DaoSession daoSession;
	private SoundSheet soundSheet;

	public StoreSoundSheetTask(DaoSession daoSession, SoundSheet soundSheet)
	{
		this.daoSession = daoSession;
		this.soundSheet = soundSheet;
	}

	@Override
	public Void call() throws Exception
	{
		this.daoSession.getSoundSheetDao().insertInTx(this.soundSheet);
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
