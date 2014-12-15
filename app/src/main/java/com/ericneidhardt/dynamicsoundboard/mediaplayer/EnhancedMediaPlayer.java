package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.broadcast.Constants;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.playlist.Playlist;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class EnhancedMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener
{
	private static final String TAG = EnhancedMediaPlayer.class.getName();

	private enum State
	{
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

	private Set<OnCompletionListener> onCompletionListeners; // listener is triggered when this.onCompletion() is called
	private LocalBroadcastManager broadcastManager;

	public EnhancedMediaPlayer(Context context, MediaPlayerData data) throws IOException
	{
		super();

		this.broadcastManager = LocalBroadcastManager.getInstance(context);
		this.onCompletionListeners = new HashSet<OnCompletionListener>();
		this.rawData = data;
		this.setLooping(data.getIsLoop());

		this.currentState = State.IDLE;
		this.init(context);
	}

	public static EnhancedMediaPlayer getInstanceForPlayList(Context context, MediaPlayerData data) throws IOException
	{
		MediaPlayerData playListData = new MediaPlayerData();
		playListData.setId(data.getId());
		playListData.setIsInPlaylist(true);
		playListData.setPlayerId(data.getPlayerId());
		playListData.setFragmentTag(Playlist.TAG);
		playListData.setIsLoop(false);
		playListData.setLabel(data.getLabel());
		playListData.setUri(data.getUri());

		return new EnhancedMediaPlayer(context, playListData);
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
		this.setOnCompletionListener(this);
	}

	public void destroy()
	{
		this.sendBroadCastSoundPlaying(false);
		this.currentState = State.DESTROYED;
		this.reset();
		this.release();
	}

	public MediaPlayerData getMediaPlayerData()
	{
		return this.rawData;
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

	public void addOnCompletionListener(OnCompletionListener listener)
	{
		this.onCompletionListeners.add(listener);
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.sendBroadCastSoundPlaying(false);
		for (OnCompletionListener listener : this.onCompletionListeners)
			listener.onCompletion(mp);
	}

	private void sendBroadCastSoundPlaying(boolean isPlaying)
	{
		Intent intent = new Intent();
		intent.setAction(Constants.ACTION_SOUND_STATE_CHANGED);
		intent.putExtra(Constants.KEY_IS_PLAYING, isPlaying);
		intent.putExtra(Constants.KEY_PLAYER_ID, this.rawData.getPlayerId());
		this.broadcastManager.sendBroadcast(intent);
	}

	public static IntentFilter getMediaPlayerIntentFilter()
	{
		return new IntentFilter(Constants.ACTION_SOUND_STATE_CHANGED);
	}

}
