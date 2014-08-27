package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;

import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class MediaPlayerPool
{
	private DaoSession daoSession;
	private List<MediaPlayer> mediaPlayers;

	public MediaPlayerPool(String poolId)
	{
		if (poolId == null)
			throw new NullPointerException("Can not create instance of MediaPlayerPool, poolId ist null");

		this.setupDatabase(DynamicSoundboardApplication.getContext(), poolId);
	}

	public void setupDatabase(Context context, String dbName)
	{
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		this.daoSession = daoMaster.newSession();
	}

	public void add(MediaPlayer mediaPlayer)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<MediaPlayer>();
		this.mediaPlayers.add(mediaPlayer);
		// TODO add MediaPlayer to database
	}

	public void addAll(List<MediaPlayer> mediaPlayers)
	{
		if (this.mediaPlayers == null)
			this.mediaPlayers = new ArrayList<MediaPlayer>();
		this.mediaPlayers.addAll(mediaPlayers);
		// TODO add all MediaPlayer to database
	}

	public void remove(MediaPlayer mediaPlayer)
	{
		if (this.mediaPlayers == null)
			throw new IllegalArgumentException("trying to remove MediaPlayer, but pool is empty");
		// TODO dispose MediaPlayer, and remove from database
		this.mediaPlayers.remove(mediaPlayer);
	}

	public void clear()
	{
		if (this.mediaPlayers == null)
			throw new IllegalArgumentException("trying to remove MediaPlayer, but pool is empty");
		// TODO dispose all MediaPlayer, and remove from database
		this.mediaPlayers.clear();
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
				MediaPlayerPool.this.mediaPlayers = mediaPlayers;
				callback.onMediaPlayersRetrieved(mediaPlayers);
			}
		};



	}

}
