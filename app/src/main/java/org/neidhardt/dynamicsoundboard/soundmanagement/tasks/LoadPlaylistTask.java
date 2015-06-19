package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerComparator;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;

import java.util.Collections;
import java.util.List;

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class LoadPlaylistTask extends LongTermTask<List<MediaPlayerData>>
{
	private static final String TAG = LoadPlaylistTask.class.getName();

	private DaoSession daoSession;
	private SoundsDataStorage soundsDataStorage;

	public LoadPlaylistTask(DaoSession daoSession, SoundsDataStorage soundsDataStorage)
	{
		this.daoSession = daoSession;
		this.soundsDataStorage = soundsDataStorage;
	}

	@Override
	public List<MediaPlayerData> call() throws Exception
	{
		List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
		Collections.sort(mediaPlayersData, new MediaPlayerComparator());
		for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
			this.soundsDataStorage.createPlaylistSoundAndAddToManager(mediaPlayerData);
		return mediaPlayersData;
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
