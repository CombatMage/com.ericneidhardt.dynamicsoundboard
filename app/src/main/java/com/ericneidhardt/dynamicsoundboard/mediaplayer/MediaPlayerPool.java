package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;

import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
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
	private List<MediaPlayer> mediaPlayers;

	public MediaPlayerPool(String poolId)
	{
		if (poolId == null)
			throw new NullPointerException("Can not create instance of MediaPlayerPool, poolId ist null");

		this.poolId = poolId;
	}

	public void add(MediaPlayer mediaPlayer)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<MediaPlayer>();
		this.mediaPlayers.add(mediaPlayer);
		SafeAsyncTask task = new StoreMediaPlayersTask(mediaPlayer);
		task.execute();
	}

	public void add(List<MediaPlayer> mediaPlayers)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<MediaPlayer>();
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
				.where(MediaPlayerDataDao.Properties.Hash.eq(mediaPlayer.hashCode()))
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

	public void getMediaPlayersAsync(final OnMediaPlayersRetrievedCallback callback)
	{
		if (this.mediaPlayers != null) {
			callback.onMediaPlayersRetrieved(this.mediaPlayers);
			return;
		}

		OnMediaPlayersRetrievedCallback callbackMediaPlayerPool = new OnMediaPlayersRetrievedCallback() {
			@Override
			public void onMediaPlayersRetrieved(List<MediaPlayer> mediaPlayers) {
				MediaPlayerPool.this.mediaPlayers.addAll(mediaPlayers);
				callback.onMediaPlayersRetrieved(MediaPlayerPool.this.mediaPlayers);
			}
		};

		SafeAsyncTask task = new LoadMediaPlayersTask(callback);
		task.execute();
	}

	private class StoreMediaPlayersTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayer> mediaPlayers;

		private StoreMediaPlayersTask(MediaPlayer mediaPlayer)
		{
			this.mediaPlayers = new ArrayList<MediaPlayer>();
			this.mediaPlayers.add(mediaPlayer);
		}

		private StoreMediaPlayersTask(List<MediaPlayer> mediaPlayers)
		{
			this.mediaPlayers = mediaPlayers;
		}

		@Override
		public Void call() throws Exception
		{
			final DaoSession daoSession = DynamicSoundboardApplication.getDatabase(poolId);
			daoSession.runInTx(new Runnable()
			{
				@Override
				public void run() {
					for (MediaPlayer mediaPlayer : mediaPlayers)
					{
						MediaPlayerData data = EnhancedMediaPlayer.getMediaPlayerData(mediaPlayer);
						daoSession.getMediaPlayerDataDao().insert(data);
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

	private class LoadMediaPlayersTask extends SafeAsyncTask<List<MediaPlayer>>
	{
		private OnMediaPlayersRetrievedCallback callback;

		private LoadMediaPlayersTask(OnMediaPlayersRetrievedCallback callback)
		{
			this.callback = callback;
		}

		@Override
		public List<MediaPlayer> call() throws Exception
		{
			DaoSession daoSession = DynamicSoundboardApplication.getDatabase(poolId);

			List<MediaPlayerData> storedMediaPlayers = daoSession.getMediaPlayerDataDao().queryBuilder().list();
			List<MediaPlayer> loadedMediaPlayers = new ArrayList<MediaPlayer>(storedMediaPlayers.size());
			for (MediaPlayerData storedMediaPlayer : storedMediaPlayers)
				loadedMediaPlayers.add(new EnhancedMediaPlayer(storedMediaPlayer));
			return loadedMediaPlayers;
		}

		@Override
		protected void onSuccess(List<MediaPlayer> mediaPlayers) throws Exception
		{
			super.onSuccess(mediaPlayers);
			if (this.callback != null)
				this.callback.onMediaPlayersRetrieved(mediaPlayers);
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG + ": " + poolId, e.getMessage());
		}
	}

}
