package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer.ExoPlayer
import com.google.android.exoplayer.FrameworkSampleSource
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer
import com.google.android.exoplayer.MediaCodecSelector
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
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
) : MediaPlayerController
{
	private val exoPlayer = ExoPlayer.Factory.newInstance(1)
	private var audioRenderer: MediaCodecAudioTrackRenderer? = null

	init { this.init() }

	private fun init()
	{
		val uri = Uri.parse(this.mediaPlayerData.uri)
		val sampleSource = FrameworkSampleSource(this.context, uri, null)
		this.audioRenderer = MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT)
	}

	override var isDeletionPending: Boolean = false

	override val isPlayingSound: Boolean = true // TODO

	override val trackDuration: Int = 1 // TODO

	override var progress: Int = 0 // TODO

	override var isLoopingEnabled: Boolean = false // TODO

	override var isInPlaylist: Boolean = false // TODO

	override fun playSound(): Boolean
	{
		this.exoPlayer.prepare(audioRenderer)
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

	override fun setSoundUri(uri: String) {
		throw UnsupportedOperationException()
	}

	override fun destroy(postStateChanged: Boolean) {
		this.exoPlayer.release()
		this.soundsDataStorage.removeSoundFromCurrentlyPlayingSounds(this)

		// TODO
	}
}