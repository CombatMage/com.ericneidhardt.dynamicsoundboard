package org.neidhardt.dynamicsoundboard.mediaplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import de.greenrobot.event.EventBus;
import org.acra.ACRA;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist;

import java.io.IOException;


public class EnhancedMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener, Runnable
{
	private static final String TAG = EnhancedMediaPlayer.class.getName();

	private static final int FADE_OUT_DURATION = 100;
	private static final int INT_VOLUME_MAX = 100;
	private static final int INT_VOLUME_MIN = 0;
	private static final float FLOAT_VOLUME_MAX = 1;
	private static final float FLOAT_VOLUME_MIN = 0;

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
	private int volume;

	private Handler handler = null;

	public EnhancedMediaPlayer(MediaPlayerData data) throws IOException
	{
		super();

		this.rawData = data;
		this.setLooping(data.getIsLoop());

		this.currentState = State.IDLE;
		this.init();
	}

	public static EnhancedMediaPlayer getInstanceForPlayList(MediaPlayerData data) throws IOException
	{
		MediaPlayerData playListData = new MediaPlayerData();
		playListData.setId(data.getId());
		playListData.setIsInPlaylist(true);
		playListData.setPlayerId(data.getPlayerId());
		playListData.setFragmentTag(Playlist.TAG);
		playListData.setIsLoop(false);
		playListData.setLabel(data.getLabel());
		playListData.setUri(data.getUri());

		return new EnhancedMediaPlayer(playListData);
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

	public void setSoundUri(String uri) throws IOException
	{
		this.rawData.setUri(uri);
		this.rawData.setItemWasUpdated();
		this.reset();

		this.init();
	}

	private void init() throws IOException
	{
		if (this.rawData.getUri() == null)
			throw new NullPointerException("cannot init media player, sound uri is null");

		this.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Uri soundUri = Uri.parse(this.rawData.getUri());
		this.setDataSource(DynamicSoundboardApplication.getSoundboardContext(), soundUri);
		this.setLooping(this.rawData.getIsLoop());
		this.prepare();
		this.currentState = State.PREPARED;

		this.volume = INT_VOLUME_MAX;
		this.duration = super.getDuration();
		this.setOnCompletionListener(this);
	}

	public void destroy(boolean postStateChanged)
	{
		if (this.handler != null)
			this.handler.removeCallbacks(this);
		this.currentState = State.DESTROYED;
		this.reset();
		this.release();
		if (postStateChanged)
			this.postStateChangedEvent(false);
	}

	@Override
	public void prepare() throws IOException, IllegalStateException {
		Logger.d(TAG, "preparing media player " + this.getMediaPlayerData().getLabel() + " with uri " + this.getMediaPlayerData().getUri());
		super.prepare();
	}

	public MediaPlayerData getMediaPlayerData()
	{
		return this.rawData;
	}

	@Override
	public int getDuration()
	{
		if (this.currentState == State.DESTROYED || this.currentState == State.IDLE)
			return 0;
		return this.duration;
	}

	/**
	 * Check if this mediaplayer is currently playingm, ie. State.STARTED.
	 * The call is not forwared to the native implementation super.isPlaying, because
	 * of ab described here:
	 * @see <a href="https://code.google.com/p/android/issues/detail?id=9732">#9732: internal/external state mismatch corrected</a>
	 * @return true if player ist playing, false otherwise
	 */
	@Override
	public boolean isPlaying()
	{
		return this.currentState == State.STARTED;
	}

	@Override
	public int getCurrentPosition()
	{
		if (this.currentState == State.DESTROYED)
			return 0;
		return super.getCurrentPosition();
	}

	@Override
	public void setLooping(boolean looping)
	{
		super.setLooping(looping);
		this.rawData.setIsLoop(looping);
		this.rawData.setItemWasAltered();
	}

	public void setIsInPlaylist(boolean inPlaylist)
	{
		this.rawData.setIsInPlaylist(inPlaylist);
		this.rawData.setItemWasAltered();
	}

	public boolean playSound()
	{
		if (this.isPlaying())
			return true;
		try
		{
			if (this.currentState == State.IDLE || this.currentState == State.DESTROYED)
				this.init();

			if (this.currentState == State.INIT || this.currentState == State.STOPPED)
				this.prepare();

			this.volume = INT_VOLUME_MAX;
			this.updateVolume(this.volume);

			this.start();
			this.currentState = State.STARTED;

			this.postStateChangedEvent(true);
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.toString());
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.toString());
			ACRA.getErrorReporter().handleException(e);
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
		if (!this.isPlaying())
			return true;
		try
		{
			switch (this.currentState)
			{
				case IDLE:
					this.init();
					this.start();
					break;
				case DESTROYED:
					this.init();
					this.start();
					break;
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

			this.postStateChangedEvent(true);
			return true;
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.getMessage());
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
	}

	public void fadeOutSound()
	{
		this.updateVolume(0);
		this.scheduleNextVolumeChange();
	}

	private void scheduleNextVolumeChange()
	{
		int delay = FADE_OUT_DURATION / INT_VOLUME_MAX;
		if (this.handler == null)
			this.handler = new Handler();
		this.handler.postDelayed(this, delay);
	}

	@Override
	public void run()
	{
		updateVolume(-1);
		if (volume == INT_VOLUME_MIN)
		{
			updateVolume(INT_VOLUME_MAX);
			pauseSound();
		}
		else
			scheduleNextVolumeChange();
	}

	private void updateVolume(int change)
	{
		this.volume = this.volume + change;

		//ensure volume within boundaries
		if (this.volume < INT_VOLUME_MIN)
			this.volume = INT_VOLUME_MIN;
		else if (this.volume > INT_VOLUME_MAX)
			this.volume = INT_VOLUME_MAX;

		//convert to float value
		float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - this.volume) / (float) Math.log(INT_VOLUME_MAX));

		//ensure fVolume within boundaries
		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN;
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX;

		this.setVolume(fVolume, fVolume);
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
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
		catch (IllegalStateException e)
		{
			Logger.e(TAG, e.getMessage());
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.postStateChangedEvent(true);
		this.postCompletedEvent();
	}

	private void postStateChangedEvent(boolean isAlive)
	{
		EventBus.getDefault().post(new MediaPlayerStateChangedEvent(this, isAlive));
	}

	private void postCompletedEvent()
	{
		EventBus.getDefault().post(new MediaPlayerCompletedEvent(this.rawData));
	}

}
