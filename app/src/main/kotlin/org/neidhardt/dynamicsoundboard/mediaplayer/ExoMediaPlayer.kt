package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer.ExoPlaybackException
import com.google.android.exoplayer.ExoPlayer
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer
import com.google.android.exoplayer.MediaCodecSelector
import com.google.android.exoplayer.extractor.ExtractorSampleSource
import com.google.android.exoplayer.upstream.DefaultAllocator
import com.google.android.exoplayer.upstream.DefaultUriDataSource
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable

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

private val RELEASE_DELAY = 10000.toLong()

private val PROGRESS_DIVIDER = 1000

private val RENDER_COUNT = 1
private val BUFFER_SEGMENT_SIZE = 64 * 1024
private val BUFFER_SEGMENT_COUNT = 160

class ExoMediaPlayer
(
	private val context: Context,
	private val eventBus: EventBus,
	private val soundsDataStorage: SoundsDataStorage,
	override val mediaPlayerData: MediaPlayerData
) : MediaPlayerController, ExoPlayer.Listener
{
	private val TAG = javaClass.name

	private val handler = EnhancedHandler()
	private val volumeController = VolumeController(this)

	private var audioRenderer: MediaCodecAudioTrackRenderer? = null
	private var exoPlayer = ExoPlayer.Factory.newInstance(RENDER_COUNT)

	private var releasePlayerSchedule: KillableRunnable? = null
	private var lastPosition: Int? = null

	init { this.init() }

	private fun init()
	{
		this.lastPosition = null

		val uriString = this.mediaPlayerData.uri ?: throw NullPointerException("$TAG: cannot init ExoMediaPlayer, given uri is null")
		val uri = Uri.parse(uriString)

		val allocator = DefaultAllocator(BUFFER_SEGMENT_SIZE)
		val dataSource = DefaultUriDataSource(context, TAG)

		val sampleSource = ExtractorSampleSource(
				uri,
				dataSource,
				allocator,
				BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT)

		this.audioRenderer = MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT)

		this.exoPlayer = ExoPlayer.Factory.newInstance(1)
		this.exoPlayer.addListener(this)
	}

	override var isDeletionPending: Boolean = false

	override val isPlayingSound: Boolean
		get() = exoPlayer.playWhenReady

	override val trackDuration: Int
		get()
		{
			if (this.exoPlayer.duration == ExoPlayer.UNKNOWN_TIME) return 0
			return (this.exoPlayer.duration / PROGRESS_DIVIDER).toInt()
		}

	override var progress: Int
		get()
		{
			if (exoPlayer.duration == ExoPlayer.UNKNOWN_TIME) return 0
			return (exoPlayer.currentPosition / PROGRESS_DIVIDER).toInt()
		}
		set(value)
		{
			val seekPosition: Int =
					if (exoPlayer.duration == ExoPlayer.UNKNOWN_TIME)
						0
					else
						Math.min(Math.max(0, value), this.trackDuration)
			exoPlayer.seekTo((seekPosition * 1000).toLong())
		}

	override var isLoopingEnabled: Boolean
		get() = this.mediaPlayerData.isLoop
		set(value)
		{
			if (value != this.mediaPlayerData.isLoop)
			{
				this.mediaPlayerData.isLoop = value
				this.mediaPlayerData.updateItemInDatabaseAsync()
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

	override var volume: Float = this.volumeController.maxVolume
		set(value)
		{
			field = value
			this.exoPlayer.sendMessage(this.audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, value);
		}

	override fun playSound(): Boolean
	{
		this.releasePlayerSchedule?.apply { handler.removeCallbacks(this) }

		this.volume = this.volumeController.maxVolume

		if (this.exoPlayer.playbackState != ExoPlayer.STATE_READY)
			this.exoPlayer.prepare(this.audioRenderer)

		this.exoPlayer.playWhenReady = true

		val lastPosition = this.lastPosition
		if (lastPosition != null)
		{
			this.progress = lastPosition
			this.lastPosition = null
		}

		this.soundsDataStorage.addSoundToCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return this.isPlayingSound
	}

	override fun stopSound(): Boolean
	{
		this.releasePlayerSchedule?.apply { handler.removeCallbacks(this) }
		this.exoPlayer.release()
		this.init()

		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		return !this.isPlayingSound
	}

	override fun pauseSound(): Boolean
	{
		this.releasePlayerSchedule = object : KillableRunnable()
		{
			override fun call()
			{
				val position = progress // remember the paused position so it can reused later
				exoPlayer.release()
				init() // init sets lastPosition to 0, therefore we set the position after ini
				lastPosition = position
			}
		}.apply { handler.postDelayed(this, RELEASE_DELAY) }


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
		this.releasePlayerSchedule?.apply { handler.removeCallbacks(this) }

		this.mediaPlayerData.uri = uri
		this.mediaPlayerData.updateItemInDatabaseAsync()

		this.init()
		this.postStateChangedEvent(true)
	}

	override fun destroy(postStateChanged: Boolean)
	{
		this.releasePlayerSchedule?.apply { handler.removeCallbacks(this) }
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
				this.exoPlayer.playWhenReady = true
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