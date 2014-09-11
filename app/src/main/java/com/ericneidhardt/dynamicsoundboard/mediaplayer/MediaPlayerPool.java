package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.media.MediaPlayer;

import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class MediaPlayerPool
{
	private static final String TAG = MediaPlayerPool.class.getSimpleName();

	private String poolId;
	private List<EnhancedMediaPlayer> mediaPlayers;

	public MediaPlayerPool(String poolId)
	{
		if (poolId == null)
			throw new NullPointerException("Can not create instance of MediaPlayerPool, poolId ist null");

		this.poolId = poolId;
	}

	/**
	 * Adds the media player to this pool and stores it in a database.
	 * @param mediaPlayer
	 */
	public void add(EnhancedMediaPlayer mediaPlayer)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();
		this.mediaPlayers.add(mediaPlayer);
		SafeAsyncTask task = new StoreMediaPlayersTask(mediaPlayer);
		task.execute();
	}

	/**
	 * Adds the raw Media Player data to storage
	 * @param data
	 */
	public void addRawData(MediaPlayerData data)
	{
		SafeAsyncTask task = new StoreMediaPlayersTask(data);
		task.execute();
	}

	/**
	 * Adds all media players to this pool and stores them in a database.
	 * @param mediaPlayers
	 */
	public void add(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();
		this.mediaPlayers.addAll(mediaPlayers);
		SafeAsyncTask task = new StoreMediaPlayersTask(mediaPlayers);
		task.execute();
	}

	public void remove(MediaPlayer mediaPlayer)
	{
		if (this.mediaPlayers == null)
			throw new IllegalArgumentException("trying to remove MediaPlayer, but pool is empty");

		mediaPlayer.release();
		this.mediaPlayers.remove(mediaPlayer);

		DaoSession daoSession = DynamicSoundboardApplication.getDatabase(this.poolId);
		daoSession.getMediaPlayerDataDao().queryBuilder()
				.where(MediaPlayerDataDao.Properties.PlayerId.eq(mediaPlayer.hashCode()))
				.buildDelete().executeDeleteWithoutDetachingEntities();
	}

	public void clear()
	{
		if (this.mediaPlayers == null)
			throw new IllegalArgumentException("trying to remove MediaPlayer, but pool is empty");

		for (MediaPlayer mediaPlayer : this.mediaPlayers)
			mediaPlayer.release();
		this.mediaPlayers.clear();

		DaoSession daoSession = DynamicSoundboardApplication.getDatabase(this.poolId);
		daoSession.getMediaPlayerDataDao().deleteAll();
	}

	public List<EnhancedMediaPlayer> getMediaPlayers()
	{
		return this.mediaPlayers;
	}

	public void getMediaPlayersAsync(final OnMediaPlayersRetrievedCallback callback)
	{
		if (this.mediaPlayers != null) {
			callback.onMediaPlayersRetrieved(this.mediaPlayers);
			return;
		}

		SafeAsyncTask task = new LoadMediaPlayersTask(callback);
		task.execute();
	}

	private class StoreMediaPlayersTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayers;

		private StoreMediaPlayersTask(EnhancedMediaPlayer mediaPlayer)
		{
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			this.mediaPlayers.add(EnhancedMediaPlayer.getMediaPlayerData(mediaPlayer));
		}

		private StoreMediaPlayersTask(MediaPlayerData mediaPlayerData)
		{
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			this.mediaPlayers.add(mediaPlayerData);
		}

		private StoreMediaPlayersTask(List<EnhancedMediaPlayer> mediaPlayers)
		{
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			for (EnhancedMediaPlayer enhancedMediaPlayer : mediaPlayers)
				this.mediaPlayers.add(EnhancedMediaPlayer.getMediaPlayerData(enhancedMediaPlayer));
		}

		@Override
		public Void call() throws Exception
		{
			final DaoSession daoSession = DynamicSoundboardApplication.getDatabase(poolId);
			daoSession.runInTx(new Runnable()
			{
				@Override
				public void run() {
					for (MediaPlayerData mediaPlayer : mediaPlayers)
					{
						daoSession.getMediaPlayerDataDao().insert(mediaPlayer);
					}
				}
			});
			return null;
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG + ": " + poolId, e.getMessage());
		}
	}

	private class LoadMediaPlayersTask extends SafeAsyncTask<List<EnhancedMediaPlayer>>
	{
		private OnMediaPlayersRetrievedCallback callback;

		private LoadMediaPlayersTask(OnMediaPlayersRetrievedCallback callback)
		{
			this.callback = callback;
		}

		@Override
		public List<EnhancedMediaPlayer> call() throws Exception
		{
			DaoSession daoSession = DynamicSoundboardApplication.getDatabase(poolId);

			List<MediaPlayerData> storedMediaPlayers = daoSession.getMediaPlayerDataDao().queryBuilder().list();
			List<EnhancedMediaPlayer> loadedMediaPlayers = new ArrayList<EnhancedMediaPlayer>(storedMediaPlayers.size());
			for (MediaPlayerData storedMediaPlayer : storedMediaPlayers)
				loadedMediaPlayers.add(new EnhancedMediaPlayer(storedMediaPlayer));
			return loadedMediaPlayers;
		}

		@Override
		protected void onSuccess(List<EnhancedMediaPlayer> mediaPlayers) throws Exception
		{
			super.onSuccess(mediaPlayers);
			if (MediaPlayerPool.this.mediaPlayers == null)
				MediaPlayerPool.this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();

			MediaPlayerPool.this.mediaPlayers.addAll(mediaPlayers);

			if (this.callback != null)
				this.callback.onMediaPlayersRetrieved(MediaPlayerPool.this.mediaPlayers);
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG + ": " + poolId, e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
