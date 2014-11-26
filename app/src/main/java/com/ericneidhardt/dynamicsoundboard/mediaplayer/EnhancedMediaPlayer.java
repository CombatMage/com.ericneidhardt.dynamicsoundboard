package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.playlist.Playlist;

import java.io.IOException;


public class EnhancedMediaPlayer extends MediaPlayer
{
	private static final String TAG = EnhancedMediaPlayer.class.getName();

	public static final String KEY_EXTRA_IS_PLAYING = "com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer.KEY_EXTRA_IS_PLAYING";
	public static final String ACTION_SOUND_STATE_CHANGED = "com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer.ACTION_SOUND_STATE_CHANGED";

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
	private int duration;

	public EnhancedMediaPlayer(Context context, MediaPlayerData data) throws IOException
	{
		super();
		this.rawData = data;
		this.setLooping(data.getIsLoop());

		this.currentState = State.IDLE;
		this.init(context);
	}

	public static EnhancedMediaPlayer getInstanceForPlayList(Context context, MediaPlayerData data) throws IOException
	{
		MediaPlayerData playListData = new MediaPlayerData();
		playListData.setIsInPlaylist(true);
		playListData.setPlayerId(data.getPlayerId());
		playListData.setFragmentTag(Playlist.TAG);
		playListData.setIsLoop(false);
		playListData.setLabel(data.getLabel());
		playListData.setUri(data.getUri());

		return new EnhancedMediaPlayer(context, playListData);
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

	private void init(Context context) throws IOException
	{
		if (this.rawData.getUri() == null)
			throw new NullPointerException("cannot init media player, sound uri is null");

		this.setAudioStreamType(AudioManager.STREAM_MUSIC);
		this.setDataSource(context, Uri.parse(this.rawData.getUri()));
		this.setLooping(this.rawData.getIsLoop());
		this.prepare();
		this.currentState = State.PREPARED;

		this.duration = super.getDuration();
	}

	public void destroy()
	{
		this.sendBroadCastSoundPlaying(false);
		this.currentState = State.DESTROYED;
		this.reset();
		this.release();
	}

	@Override
	public int getDuration()
	{
		return this.duration;
	}

	@Override
	public boolean isPlaying()
	{
		return currentState != State.DESTROYED && super.isPlaying();
	}

	@Override
	public void setLooping(boolean looping)
	{
		super.setLooping(looping);
		this.rawData.setIsLoop(looping);
	}

	public void setIsInPlaylist(boolean inPlaylist)
	{
		this.rawData.setIsInPlaylist(inPlaylist);
	}

	public boolean playSound()
	{
		try
		{
			if (this.currentState == State.INIT || this.currentState == State.STOPPED)
				this.prepare();

			this.sendBroadCastSoundPlaying(true);
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
		if (this.pauseSound())
		{
			this.seekTo(0);
			return true;
		}
		return false;
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
			this.sendBroadCastSoundPlaying(false);
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

	private void sendBroadCastSoundPlaying(boolean isPlaying)
	{
		Intent intent = new Intent();
		intent.setAction(ACTION_SOUND_STATE_CHANGED);
		intent.putExtra(KEY_EXTRA_IS_PLAYING, isPlaying);
		DynamicSoundboardApplication.getContext().sendBroadcast(intent);
	}

}
