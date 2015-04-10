package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsLoadedEvent;

import java.util.List;

/**
 * Created by eric.neidhardt on 10.04.2015.
 */
public class LoadSoundsTask extends LoadTask<MediaPlayerData>
{
	private DaoSession daoSession;

	public LoadSoundsTask(DaoSession daoSession)
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
			bus.post(new SoundsLoadedEvent(mediaPlayerData));
		}
		return mediaPlayersData;
	}
}
