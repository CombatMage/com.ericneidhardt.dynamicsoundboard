package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage

/**
 * File created by eric.neidhardt on 11.04.2016.
 */
fun getNewMediaPlayerController(context: Context,
								eventBus: EventBus,
								mediaPlayerData: MediaPlayerData,
								soundsDataStorage: SoundsDataStorage): MediaPlayerController
{
	return ExoMediaPlayer(context, eventBus, soundsDataStorage, mediaPlayerData)
}

class ExoMediaPlayer
(
	private val context: Context,
	private val eventBus: EventBus,
	private val soundsDataStorage: SoundsDataStorage,
	override val mediaPlayerData: MediaPlayerData
) : MediaPlayerController, ExoPlayer.Listener
{
	private val TAG = javaClass.name

	private val volumeController = VolumeController(this)
	private var audioRenderer: MediaCodecAudioTrackRenderer? = null
	private var exoPlayer = ExoPlayer.Factory.newInstance(1)

	init { this.init() }

	private fun init()
	{
		val uri = Uri.parse(this.mediaPlayerData.uri)

		val sampleSource = FrameworkSampleSource(this.context, uri, null)
		this.audioRenderer = MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT)

		this.exoPlayer = ExoPlayer.Factory.newInstance(1)
		this.exoPlayer.addListener(this)
		//this.exoPlayer.prepare(audioRenderer)
	}

	override var isDeletionPending: Boolean = false

	override val isPlayingSound: Boolean
		get() = exoPlayer.playWhenReady

	override val trackDuration: Int
		get()
		{
			if (this.exoPlayer.duration == ExoPlayer.UNKNOWN_TIME) return 0
			return this.exoPlayer.duration.toInt()
		}

	override var progress: Int
		get()
		{
			if (exoPlayer.duration == ExoPlayer.UNKNOWN_TIME) return 0
			return exoPlayer.currentPosition.toInt()
		}
		set(value)
		{
			val seekPosition: Long =
					if (exoPlayer.duration == ExoPlayer.UNKNOWN_TIME)
						0
					else
						Math.min(Math.max(0, value), this.trackDuration).toLong()
			exoPlayer.seekTo(seekPosition)
		}

	override var isLoopingEnabled: Boolean = false

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

	override var volume: Float = this.volumeController.maxVolume
		set(value)
		{
			field = value
			this.exoPlayer.sendMessage(this.audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, value);
		}

	override fun playSound(): Boolean
	{
		// TODO stop deletion timer
		this.volume = this.volumeController.maxVolume
		this.exoPlayer.prepare(this.audioRenderer)
		this.exoPlayer.playWhenReady = true

		this.soundsDataStorage.addSoundToCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return this.isPlayingSound
	}

	override fun stopSound(): Boolean
	{
		// TODO stop deletion timer
		this.exoPlayer.release()
		this.init()

		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return !this.isPlayingSound
	}

	override fun pauseSound(): Boolean
	{
		// TODO start deletion timmer
		this.exoPlayer.playWhenReady = false

		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return !this.isPlayingSound
	}

	override fun fadeOutSound()
	{
		this.volumeController.fadeOutSound()
	}

	override fun setSoundUri(uri: String)
	{
		// TODO stop deletion timer
		this.mediaPlayerData.uri = uri
		this.mediaPlayerData.updateItemInDatabaseAsync()

		this.init()
		this.postStateChangedEvent(true)
	}

	override fun destroy(postStateChanged: Boolean)
	{
		// TODO stop deletion timer
		this.volumeController.cancelFadeOut()
		this.exoPlayer.release()
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		if (postStateChanged)
			this.postStateChangedEvent(false)
	}

	override fun onPlayerError(error: ExoPlaybackException)
	{
		Logger.e(TAG, "onPlayerError for $this with exception $error")
		this.init()
		this.eventBus.post(MediaPlayerFailedEvent(this, PlayerAction.UNDEFINED))
	}

	override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)
	{
		if (playbackState == ExoPlayer.STATE_ENDED)
		{
			if (this.isLoopingEnabled)
			{
				this.progress = 0
				this.playSound()
			}
			else
				this.onCompletion()
		}

		this.postStateChangedEvent(true)
	}

	private fun onCompletion()
	{
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.eventBus.post(MediaPlayerCompletedEvent(this))
	}

	override fun onPlayWhenReadyCommitted()
	{
		this.postStateChangedEvent(true)
	}

	private fun postStateChangedEvent(isAlive: Boolean): Unit = this.eventBus.post(MediaPlayerStateChangedEvent(this, isAlive))
}