package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;

import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;

import java.io.IOException;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class EnhancedMediaPlayer extends MediaPlayer {

	private static final String TAG = EnhancedMediaPlayer.class.getSimpleName();

	private enum State {
		IDLE,
		INIT,
		PREPARED,
		STARTED,
		STOPPED,
		PAUSED
	}

	private State currentState;

	public EnhancedMediaPlayer(Uri uri, String label)
	{
		super();
		// TODO build player from params
	}

	public EnhancedMediaPlayer(MediaPlayerData data)
	{
		super();
		// TODO build player from greendao object
	}

	public static MediaPlayerData getMediaPlayerData(MediaPlayer mediaPlayer)
	{
		// TODO build greendao object from player
		return new MediaPlayerData();
	}

	public static MediaPlayerData getMediaPlayerData(Uri soundUri, String soundLabel)
	{
		// TODO build greendao object from player
		return new MediaPlayerData();
	}

	public boolean playSound()
	{
		try
		{
			if (this.currentState == State.INIT || this.currentState == State.STOPPED)
				this.prepare();
			this.start();
			this.currentState = State.STARTED;
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.toString());
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.toString());
			return false;
		}
	}

	public boolean stopSound()
	{
		try
		{
			if (this.currentState == State.INIT)
				this.prepare();
			this.stop();
			this.prepare();
			this.seekTo(0);
			this.currentState = State.PREPARED;
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.toString());
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.toString());
			return false;
		}
	}

	public boolean pauseSound()
	{
		try
		{
			switch (this.currentState)
			{
				case INIT:
					this.prepare();
					this.start();
					break;
				case PREPARED:
					this.start();
					break;
				case STARTED:
					break;
				case PAUSED:
					break;
				case STOPPED:
					this.prepare();
					this.start();
					break;
				default:
					return false; // should not be reached
			}
			this.pause();
			this.currentState = State.PAUSED;
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}
}
