package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;

import java.util.List;

/**
 * Created by eric.neidhardt on 10.04.2015.
 */
public class LoadPlaylistTask extends LoadTask<MediaPlayerData>
{
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
			super.postOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{

						bus.post(new PlayListLoadedEvent(mediaPlayerData));
				}
			});
		}
		return mediaPlayersData;
	}
}
