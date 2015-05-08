package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.misc.LongTermTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 10.04.2015.
 */
public class LoadPlaylistTask extends LongTermTask<MediaPlayerData>
{
	private static final String TAG = LoadPlaylistTask.class.getName();

	private DaoSession daoSession;

	public LoadPlaylistTask(DaoSession daoSession)
	{
		this.daoSession = daoSession;
	}

	@Override
	public List<MediaPlayerData> call() throws Exception
	{
		List<MediaPlayerData> mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list();
		final EventBus bus = EventBus.getDefault();
		for (final MediaPlayerData mediaPlayerData : mediaPlayersData)
		{
			bus.post(new PlayListLoadedEvent(mediaPlayerData, true));
		}
		return mediaPlayersData;
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
