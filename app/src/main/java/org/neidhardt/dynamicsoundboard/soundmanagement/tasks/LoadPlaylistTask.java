package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;

import java.util.List;

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class LoadPlaylistTask extends LongTermTask<List<MediaPlayerData>>
{
	private static final String TAG = LoadPlaylistTask.class.getName();

	private EventBus eventBus;

	private DaoSession daoSession;
	private SoundsDataAccess soundsDataAccess;
	private SoundsDataStorage soundsDataStorage;
	private SoundsDataUtil soundsDataUtil;

	public LoadPlaylistTask(DaoSession daoSession, SoundsDataAccess soundsDataAccess, SoundsDataStorage soundsDataStorage, SoundsDataUtil soundsDataUtil)
	{
		this.daoSession = daoSession;
		this.soundsDataAccess = soundsDataAccess;
		this.soundsDataStorage = soundsDataStorage;
		this.soundsDataUtil = soundsDataUtil;

		this.eventBus = EventBus.getDefault();
	}

	@Override
	public List<MediaPlayerData> call() throws Exception
	{
		List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
		for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
		{
			this.createPlaylistSoundAndAddToManager(mediaPlayerData);
		}
		return mediaPlayersData;
	}

	private void createPlaylistSoundAndAddToManager(MediaPlayerData data)
	{
		if (this.soundsDataAccess.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = this.soundsDataUtil.createPlaylistSound(data);
		if (player == null)
		{
			this.soundsDataStorage.removePlaylistDataFromDatabase(data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
		}
		else
			this.soundsDataStorage.addSoundToPlayList(player);
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
