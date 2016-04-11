package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
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
	private val exoPlayer = ExoPlayer.Factory.newInstance(1)
	private var audioRenderer: MediaCodecAudioTrackRenderer? = null

	init { this.init() }

	private fun init()
	{
		val uri = Uri.parse(this.mediaPlayerData.uri)

		val sampleSource = FrameworkSampleSource(this.context, uri, null)
		this.audioRenderer = MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT)

		this.exoPlayer.addListener(this)
		this.exoPlayer.prepare(audioRenderer)
	}

	override var isDeletionPending: Boolean = false

	override val isPlayingSound: Boolean = exoPlayer.playWhenReady

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

	override fun playSound(): Boolean
	{
		this.exoPlayer.playWhenReady = true
		return true
	}

	override fun stopSound(): Boolean
	{
		this.exoPlayer.stop()
		this.exoPlayer.seekTo(0)
		return true
	}

	override fun pauseSound(): Boolean
	{
		this.exoPlayer.playWhenReady = false
		return true
	}

	override fun fadeOutSound() {
		// TODO
		this.exoPlayer.playWhenReady = false
	}

	override fun setSoundUri(uri: String)
	{
		this.mediaPlayerData.uri = uri
		this.mediaPlayerData.updateItemInDatabaseAsync()

		this.init()
		this.postStateChangedEvent(true)
	}

	override fun destroy(postStateChanged: Boolean)
	{
		this.exoPlayer.release()
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		if (postStateChanged)
			this.postStateChangedEvent(false)
	}

	override fun onPlayerError(error: ExoPlaybackException?) {
		// TODO
	}

	override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
		if (playbackState == ExoPlayer.STATE_ENDED){
			if (this.isLoopingEnabled) {
				this.progress = 0
				this.playSound()
			}
			else
				this.onCompletion()
		}
	}

	private fun onCompletion()
	{
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.eventBus.post(MediaPlayerCompletedEvent(this))
	}

	override fun onPlayWhenReadyCommitted() {}

	private fun postStateChangedEvent(isAlive: Boolean): Unit = this.eventBus.post(MediaPlayerStateChangedEvent(this, isAlive))
}