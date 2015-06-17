package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;

import java.util.List;

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class LoadSoundsTask extends LongTermTask<List<MediaPlayerData>>
{
	private static final String TAG = LoadSoundsTask.class.getName();

	private DaoSession daoSession;
	private SoundsDataStorage soundsDataStorage;

	public LoadSoundsTask(DaoSession daoSession, SoundsDataStorage soundsDataStorage)
	{
		this.daoSession = daoSession;
		this.soundsDataStorage = soundsDataStorage;
	}

	@Override
	public List<MediaPlayerData> call() throws Exception
	{
		List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
		for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
			this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData);

		return mediaPlayersData;
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
