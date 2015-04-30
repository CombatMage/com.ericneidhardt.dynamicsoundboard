package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import org.acra.ACRA;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by eric.neidhardt on 10.04.2015.
 */
public class UpdateSoundsTask extends SafeAsyncTask<Void>
{
	private static final String TAG = UpdateSoundsTask.class.getName();

	private List<MediaPlayerData> mediaPlayers;
	private DaoSession database;

	/**
	 * Update stored sound database
	 * @param mediaPlayers map of media players currently loaded in corresponding sound sheets
	 * @param database daoSession to store data
	 */
	public UpdateSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession database)
	{
		this.database = database;
		this.mediaPlayers = new ArrayList<>();
		for (String fragmentTag : mediaPlayers.keySet())
		{
			List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
			for (EnhancedMediaPlayer player : playersOfFragment)
				this.mediaPlayers.add(player.getMediaPlayerData());
		}
	}

	/**
	 * Update stored playlist database
	 * @param mediaPlayers list of media players currently loaded in playlist
	 * @param database daoSession to store data
	 */
	public UpdateSoundsTask(List<EnhancedMediaPlayer> mediaPlayers, DaoSession database)
	{
		this.database = database;
		this.mediaPlayers = new ArrayList<>();
		for (EnhancedMediaPlayer player : mediaPlayers)
			this.mediaPlayers.add(player.getMediaPlayerData());
	}

	@Override
	public Void call() throws Exception
	{
		this.database.runInTx(new Runnable()
		{
			@Override
			public void run()
			{
				MediaPlayerDataDao dao = database.getMediaPlayerDataDao();
				for (MediaPlayerData playerToUpdate : mediaPlayers)
				{
					List<MediaPlayerData> storePlayers = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerToUpdate.getPlayerId())).list();
					int count = storePlayers.size();
					if (count == 0)
						dao.insert(playerToUpdate);
					else if (count == 1 && playerToUpdate.wasAlteredAfterLoading())
					{
						MediaPlayerData storedPlayer = storePlayers.get(0); // the player id should be unique so there should be no more than one entry
						updateStorePlayerData(storedPlayer, playerToUpdate);
						dao.update(storedPlayer);
					}
					else
					{
						String message = "More than one matching entry in dao found " + playerToUpdate;
						Logger.e(TAG, message);
						ACRA.getErrorReporter().handleException(new IllegalStateException(message));
					}
				}
			}
		});
		return null;
	}

	private void updateStorePlayerData(MediaPlayerData storedPlayer, MediaPlayerData newPlayerData)
	{
		storedPlayer.setFragmentTag(newPlayerData.getFragmentTag());
		storedPlayer.setIsInPlaylist(newPlayerData.getIsInPlaylist());
		storedPlayer.setIsLoop(newPlayerData.getIsLoop());
		storedPlayer.setLabel(newPlayerData.getLabel());
		storedPlayer.setTimePosition(newPlayerData.getTimePosition());

		storedPlayer.setItemWasUpdated();
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
