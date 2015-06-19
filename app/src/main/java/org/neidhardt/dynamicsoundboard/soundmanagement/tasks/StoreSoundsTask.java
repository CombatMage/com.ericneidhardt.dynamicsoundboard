package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import roboguice.util.SafeAsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class StoreSoundsTask extends SafeAsyncTask<Void>
{
	private static final String TAG = StoreSoundsTask.class.getName();

	private List<MediaPlayerData> mediaPlayers;
	private DaoSession daoSession;

	/**
	 * Update stored sound daoSession
	 * @param mediaPlayers map of media players currently loaded in corresponding sound sheets
	 * @param daoSession daoSession to store data
	 */
	public StoreSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession daoSession)
	{
		this.daoSession = daoSession;
		this.mediaPlayers = new ArrayList<>();
		for (String fragmentTag : mediaPlayers.keySet())
		{
			List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
			for (EnhancedMediaPlayer player : playersOfFragment)
				this.mediaPlayers.add(player.getMediaPlayerData());
		}
	}

	/**
	 * Update stored playlist daoSession
	 * @param mediaPlayers list of media players currently loaded in playlist
	 * @param daoSession daoSession to store data
	 */
	public StoreSoundsTask(List<EnhancedMediaPlayer> mediaPlayers, DaoSession daoSession)
	{
		this.daoSession = daoSession;
		this.mediaPlayers = new ArrayList<>();
		for (EnhancedMediaPlayer player : mediaPlayers)
			this.mediaPlayers.add(player.getMediaPlayerData());
	}

	@Override
	public Void call() throws Exception
	{
		this.daoSession.getMediaPlayerDataDao().insertInTx(this.mediaPlayers);
		return null;
	}

	@Override
	protected void onException(Exception e) throws RuntimeException
	{
		super.onException(e);
		Logger.e(TAG, e.getMessage());
		throw new RuntimeException(e);
	}

	@Override
	protected void onInterrupted(Exception e)
	{
		super.onInterrupted(e);
	}
}
