package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;

import java.io.IOException;


public class EnhancedMediaPlayer extends MediaPlayer {

	private static final String TAG = EnhancedMediaPlayer.class.getSimpleName();

	private enum State {
		IDLE,
		INIT,
		PREPARED,
		STARTED,
		STOPPED,
		PAUSED,
		DESTROYED
	}

	private State currentState;
	private MediaPlayerData rawData;

	public EnhancedMediaPlayer(Context context, MediaPlayerData data)
	{
		super();
		this.rawData = data;
		this.setLooping(data.getIsLoop());

		this.currentState = State.IDLE;
		this.init(context);

		// TODO set time position from green dao object
	}

	public EnhancedMediaPlayer(Context context, MediaPlayerData data, boolean forPlayList)
	{
		super();
		this.rawData = data;
		this.currentState = State.IDLE;

		if (!forPlayList)
		{
			this.setLooping(data.getIsLoop());
			// TODO set time position from green dao object
		}

		this.init(context);
	}

	public MediaPlayerData getMediaPlayerData()
	{
		return this.rawData;
	}

	public static MediaPlayerData getMediaPlayerData(String fragmentTag, Uri uri, String label)
	{
		MediaPlayerData data = new MediaPlayerData();

		String playerId = Integer.toString((uri.toString() + DynamicSoundboardApplication.getRandomNumber()).hashCode());
		data.setPlayerId(playerId);
		data.setFragmentTag(fragmentTag);
		data.setLabel(label);
		data.setUri(uri.toString());
		data.setIsInPlaylist(false);
		data.setIsLoop(false);

		return data;
	}

	private void init(Context context)
	{
		if (this.rawData.getUri() == null)
			throw new NullPointerException("cannot init media player, sound uri is null");

		try
		{
			this.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.setDataSource(context, Uri.parse(this.rawData.getUri()));
			this.setLooping(this.rawData.getIsLoop());
			this.prepare();
			this.currentState = State.PREPARED;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void destroy()
	{
		this.currentState = State.DESTROYED;
		this.reset();
		this.release();
	}

	@Override
	public void setLooping(boolean looping)
	{
		super.setLooping(looping);
		this.rawData.setIsLoop(looping);
	}

	public void setInPlaylist(boolean inPlaylist)
	{
		this.rawData.setIsInPlaylist(inPlaylist);
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

	public boolean setPositionTo(int timePosition)
	{
		try
		{
			switch (this.currentState)
			{
				case INIT:
					this.prepare();
					this.seekTo(timePosition);
					this.currentState = State.PREPARED;
					return true;
				case PREPARED:
					this.seekTo(timePosition);
					return true;
				case STARTED:
					this.seekTo(timePosition);
					return true;
				case PAUSED:
					this.seekTo(timePosition);
					return true;
				case STOPPED:
					this.prepare();
					this.seekTo(timePosition);
					this.currentState = State.PREPARED;
					return true;
				default:
					return false;
			}
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
