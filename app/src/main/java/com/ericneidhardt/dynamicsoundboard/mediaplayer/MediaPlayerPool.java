package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class MediaPlayerPool
{

	private static MediaPlayerPool instance;

	private Context context;
	private List<MediaPlayer> mediaPlayers;

	public static MediaPlayerPool getInstance(Context context)
	{
		if (instance == null)
			instance = new MediaPlayerPool();
		instance.context = context;
		return instance;
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

	public void getMediaPlayersAsync(OnMediaPlayersRetrievedCallback callback)
	{
		if (this.mediaPlayers != null)
		{
			if (callback != null)
				callback.onMediaPlayersRetrieved(this.mediaPlayers);
		}

	}
}
