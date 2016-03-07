package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable
import java.io.IOException

val PlaylistTAG = "PlaylistTAG"

private val FADE_OUT_DURATION = 100
private val INT_VOLUME_MAX = 100
private val INT_VOLUME_MIN = 0
private val FLOAT_VOLUME_MAX = 1f
private val FLOAT_VOLUME_MIN = 0f

private val DURATION_NOT_SET = -1;

enum class PlayerAction
{
	PLAY,
	PAUSE,
	PROGRESS,
	UNDEFINDED
}

fun getNewMediaPlayerController(context: Context,
								eventBus: EventBus,
								mediaPlayerData: MediaPlayerData,
								soundsDataStorage: SoundsDataStorage): MediaPlayerController
{
	return ImprovedMediaPlayer(context, eventBus, mediaPlayerData, soundsDataStorage)
}

private class ImprovedMediaPlayer
(
		private val context: Context,
		private val eventBus: EventBus,
		override val mediaPlayerData: MediaPlayerData,
		private val soundsDataStorage: SoundsDataStorage
) :
		VerboseMediaPlayer(),
		MediaPlayerController,
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener,
		MediaPlayer.OnInfoListener
{

	private val TAG = javaClass.name

	private var volume: Int = 0
	private var handler: EnhancedHandler? = null
	private var fadeOutSchedule: KillableRunnable? = null

	override val trackDuration: Int
		get()
		{
			if (this.currentState == State.INIT
					|| this.currentState == State.IDLE
					|| this.currentState == State.ERROR
					|| this.currentState == State.DESTROYED)
				return DURATION_NOT_SET
			return this.duration
		}

	override val isPlayingSound: Boolean
		get()
		{
			if (this.currentState == State.ERROR || this.currentState == State.DESTROYED)
				return false
			return this.isPlaying
		}

	override var progress: Int
		get()
		{
			if (this.currentState == State.ERROR || this.currentState == State.DESTROYED)
				return 0
			return this.currentPosition
		}
		set(value)
		{
			if (this.currentState == State.INIT
					|| this.currentState == State.IDLE
					|| this.currentState == State.ERROR
					|| this.currentState == State.DESTROYED)
				Logger.e(TAG, "SetProgress called in invalid state for player $this")
			else
				this.seekTo(value)
		}

	override var isLoopingEnabled: Boolean
		get()
		{
			val isLooping = this.isLooping
			if (isLooping != this.mediaPlayerData.isLoop)
			{
				Logger.e(TAG, "GetLooping detected mismatch between player and data for player $this")
				this.mediaPlayerData.isLoop = isLooping
				this.mediaPlayerData.updateItemInDatabaseAsync()
			}
			return isLooping
		}
		set(value)
		{
			if (this.currentState == State.ERROR || this.currentState == State.DESTROYED)
				Logger.e(TAG, "SetLooping called in invalid state for player $this")
			else
			{
				this.isLooping = value
				if (this.mediaPlayerData.isLoop != value)
				{
					this.mediaPlayerData.isLoop = value
					this.mediaPlayerData.updateItemInDatabaseAsync()
				}
			}
		}

	override var isInPlaylist: Boolean
		get() = this.mediaPlayerData.isInPlaylist
		set(value)
		{
			if (value != this.mediaPlayerData.isInPlaylist)
			{
				this.mediaPlayerData.isInPlaylist = value
				this.mediaPlayerData.updateItemInDatabaseAsync()
			}
		}

	init
	{
		this.setOnErrorListener(this)
		this.setOnInfoListener(this)
		this.setOnCompletionListener(this)

		this.isLoopingEnabled = mediaPlayerData.isLoop

		this.init(context)
	}

	@Throws(IOException::class)
	private fun init(context: Context)
	{
		if (this.mediaPlayerData.uri == null)
			throw NullPointerException("cannot initIfRequired media player, sound uri is null")

		this.setAudioStreamType(AudioManager.STREAM_MUSIC)
		this.setDataSource(context, Uri.parse(this.mediaPlayerData.uri))
		this.isLoopingEnabled = this.mediaPlayerData.isLoop
		this.volume = INT_VOLUME_MAX
	}

	override fun setSoundUri(uri: String)
	{
		this.mediaPlayerData.uri = uri
		this.mediaPlayerData.updateItemInDatabaseAsync()
		this.reset()

		this.init(SoundboardApplication.context)
		this.postStateChangedEvent(true)
	}

	override fun playSound(): Boolean
	{
		if (this.currentState == State.INIT)
			this.prepare()

		if (this.currentState == State.INIT
				|| this.currentState == State.IDLE
				|| this.currentState == State.ERROR
				|| this.currentState == State.DESTROYED)
		{
			Logger.e(TAG, "playSound called in invalid state for player $this")
			return false
		}

		this.volume = INT_VOLUME_MAX
		this.updateVolume(this.volume)

		this.start()

		this.soundsDataStorage.addSoundToCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)

		return true
	}

	override fun pauseSound(): Boolean {
		if (this.currentState == State.INIT
				|| this.currentState == State.IDLE
				|| this.currentState == State.STOPPED
				|| this.currentState == State.ERROR
				|| this.currentState == State.DESTROYED)
		{
			Logger.e(TAG, "pauseSound called in invalid state for player $this")
			return false
		}

		this.pause()
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return true
	}

	override fun stopSound(): Boolean
	{
		if (this.pauseSound())
		{
			this.seekTo(0)
			this.release()
			this.init(SoundboardApplication.context)
			return true
		}
		return false
	}

	override fun fadeOutSound()
	{
		this.updateVolume(0)
		this.fadeOutSchedule?.apply { handler?.removeCallbacks(this) }
		this.scheduleNextVolumeChange()
	}

	private fun scheduleNextVolumeChange()
	{
		val delay = FADE_OUT_DURATION / INT_VOLUME_MAX
		if (this.handler == null)
			this.handler = EnhancedHandler()

		this.fadeOutSchedule = object : KillableRunnable
		{
			@Volatile override var isKilled: Boolean = false

			override fun run()
			{
				if (!this.isKilled)
					scheduleNexFadeOutIteration()
			}
		}.apply { handler?.postDelayed(this, delay.toLong()) }
	}

	private fun scheduleNexFadeOutIteration()
	{
		updateVolume(-1)
		if (volume == INT_VOLUME_MIN)
		{
			updateVolume(INT_VOLUME_MAX)
			pauseSound()
		}
		else
			scheduleNextVolumeChange()
	}

	private fun updateVolume(change: Int)
	{
		this.volume = this.volume + change

		//ensure volume within boundaries
		if (this.volume < INT_VOLUME_MIN)
			this.volume = INT_VOLUME_MIN
		else if (this.volume > INT_VOLUME_MAX)
			this.volume = INT_VOLUME_MAX

		//convert to float value
		var fVolume = 1 - (Math.log((INT_VOLUME_MAX - this.volume).toDouble()).toFloat() / Math.log(INT_VOLUME_MAX.toDouble()).toFloat())

		//ensure fVolume within boundaries
		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX

		this.setVolume(fVolume, fVolume)
	}

	override fun destroy(postStateChanged: Boolean)
	{
		this.fadeOutSchedule?.apply { handler?.removeCallbacks(this) }
		this.release()
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		if (postStateChanged)
			this.postStateChangedEvent(false)
	}

	override fun onCompletion(mp: MediaPlayer?)
	{
		// for unknown reason, onCompletion is called even if the player is set to looping, therefore we needs to do an additional check
		if (this.isLoopingEnabled)
			return

		// for unknown reason, this must be set to paused instead of stopped.
		// This contradicts MediaPlayer Documentation, but calling prepare for restart throws illegal state exception
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.eventBus.post(MediaPlayerCompletedEvent(this))
	}

	override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean
	{
		Logger.e(TAG, "onError(" + mp.toString() + ") what: " + what + " extra: " + extra)
		this.reset()
		this.init(this.context)
		this.eventBus.post(MediaPlayerFailedEvent(this, PlayerAction.UNDEFINDED))
		return true
	}

	override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean
	{
		Logger.d(TAG, "onInfo(" + mp.toString() + ") what: " + what + " extra: " + extra)
		return true
	}

	private fun postStateChangedEvent(isAlive: Boolean)
	{
		this.eventBus.post(MediaPlayerStateChangedEvent(this, isAlive))
	}
}