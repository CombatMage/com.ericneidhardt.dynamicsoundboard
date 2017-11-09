package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.enhancedhandler.EnhancedHandler
import org.neidhardt.enhancedhandler.KillableRunnable
import org.neidhardt.app_utils.letThis
import java.io.File
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 11.04.2016.
 */
object MediaPlayerFactory {

	private val TAG = javaClass.name

	fun createPlayer(context: Context, eventBus: EventBus, playerData: MediaPlayerData) : MediaPlayerController? {
		try {
			val file = Uri.parse(playerData.uri).getFileForUri()
			if (file == null || !file.isAudioFile)
				throw Exception("cannot create create media player, given file is no audio file")

			return getNewMediaPlayerController (
					context = context,
					eventBus = eventBus,
					mediaPlayerData = playerData,
					manager = SoundboardApplication.soundLayoutManager
			)
		}
		catch (e: Exception) {
			Logger.d(TAG, playerData.toString() + " " + e.message)
			return null
		}
	}

	private fun getNewMediaPlayerController(context: Context,
											eventBus: EventBus,
											mediaPlayerData: MediaPlayerData,
											manager: SoundLayoutManager): MediaPlayerController =
			ExoMediaPlayer(context, eventBus, manager, mediaPlayerData)

	fun getNewMediaPlayerData(fragmentTag: String, uri: Uri, label: String): MediaPlayerData {
		val data = MediaPlayerData()

		data.playerId = Integer.toString((uri.toString() + SoundboardApplication.randomNumber).hashCode())
		data.fragmentTag = fragmentTag
		data.label = label
		data.uri = uri.toString()
		data.isLoop = false

		return data
	}

	fun getMediaPlayerDataFromFile(file: File, fragmentTag: String): MediaPlayerData {
		val soundUri = Uri.parse(file.absolutePath)
		val soundLabel = FileUtils.stripFileTypeFromName(
				FileUtils.getFileNameFromUri(SoundboardApplication.context, soundUri))
		return MediaPlayerFactory.getNewMediaPlayerData(fragmentTag, soundUri, soundLabel)
	}
}


val PlaylistTAG = "PlaylistTAG"

private val RELEASE_DELAY = 10000.toLong()

private val PROGRESS_DIVIDER = 1000

enum class PlayerAction
{
	PLAY,
	PAUSE,
	PROGRESS,
	UNDEFINED
}

val UPDATE_INTERVAL: Long = 500

class ExoMediaPlayer
(
		private val context: Context,
		private val eventBus: EventBus,
		private val manager: SoundLayoutManager,
		override val mediaPlayerData: MediaPlayerData
) : MediaPlayerController, ExoPlayer.EventListener
{
	private val TAG = javaClass.name

	private val TIME_UNKNOWN = C.TIME_UNSET

	private val enhancedHandler = EnhancedHandler()
	private val handler = Handler()

	private val volumeController = VolumeController(this)
	private val progressMonitor = ProgressMonitor(this)

	private var exoPlayer by Delegates.notNull<SimpleExoPlayer>()
	private var audioSource: MediaSource? = null

	private var releasePlayerSchedule: KillableRunnable? = null
	private var lastPosition: Int? = null

	override var mOnProgressChangedEventListener: MediaPlayerController.OnProgressChangedEventListener?
		get() = this.progressMonitor.onProgressChangedEventListener
		set(value) { this.progressMonitor.onProgressChangedEventListener = value }

	init { this.init() }

	private fun init() {
		this.lastPosition = null

		val uriString = this.mediaPlayerData.uri ?: throw NullPointerException("$TAG: cannot init ExoMediaPlayer, given uri is null")
		val uri = Uri.parse(uriString)

		val dataSourceFactory = DefaultDataSourceFactory(this.context, context.getString(R.string.app_name), DefaultBandwidthMeter())
		val extractorFactory = DefaultExtractorsFactory()
		this.audioSource = ExtractorMediaSource(uri, dataSourceFactory, extractorFactory, null, null)

		this.exoPlayer = ExoPlayerFactory.newSimpleInstance(this.context, DefaultTrackSelector(this.handler), DefaultLoadControl())
		this.exoPlayer.addListener(this)
	}

	override var isDeletionPending: Boolean = false

	override val isPlayingSound: Boolean
		get() = exoPlayer.playWhenReady

	override val isFadingOut: Boolean
		get() = this.volumeController.isFadeoutInProgress

	override val albumCover: ByteArray? by lazy {
		MediaMetadataRetriever().let {
			it.setDataSource(this.context, Uri.parse(this.mediaPlayerData.uri))
			it.embeddedPicture
		}
	}

	override val trackDuration: Int
		get() {
			if (this.exoPlayer.duration == TIME_UNKNOWN) return 0
			return (this.exoPlayer.duration / PROGRESS_DIVIDER).toInt()
		}

	override var progress: Int
		get() {
			if (exoPlayer.duration == TIME_UNKNOWN) return 0
			return (exoPlayer.currentPosition / PROGRESS_DIVIDER).toInt()
		}
		set(value) {
			val seekPosition: Int =
					if (exoPlayer.duration == TIME_UNKNOWN)
						0
					else
						Math.min(Math.max(0, value), this.trackDuration)
			exoPlayer.seekTo((seekPosition * 1000).toLong())
		}

	override var isLoopingEnabled: Boolean
		get() = this.mediaPlayerData.isLoop
		set(value) {
			if (value != this.mediaPlayerData.isLoop) {
				this.mediaPlayerData.isLoop = value
			}
		}

	override var volume: Float = this.volumeController.maxVolume
		set(value) {
			field = value
			this.exoPlayer.volume = value
		}

	override fun playSound(): Boolean {
		this.releasePlayerSchedule?.let { this.enhancedHandler.removeCallbacks(it) }

		this.volume = this.volumeController.maxVolume

		if (this.exoPlayer.playbackState != ExoPlayer.STATE_READY)
			this.exoPlayer.prepare(this.audioSource)

		this.exoPlayer.playWhenReady = true

		val lastPosition = this.lastPosition
		if (lastPosition != null)
			this.progress = lastPosition
		else
			this.lastPosition = null

		this.manager.addSoundToCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.progressMonitor.startProgressUpdateTimer()
		return this.isPlayingSound
	}

	override fun stopSound(): Boolean {
		this.releasePlayerSchedule?.let { this.enhancedHandler.removeCallbacks(it) }
		this.exoPlayer.release()
		this.init()

		this.manager.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.progressMonitor.stopProgressUpdateTimer()

		return !this.isPlayingSound
	}

	override fun pauseSound(): Boolean {
		this.releasePlayerSchedule = KillableRunnable({
			val position = progress // remember the paused position so it can reused later
			exoPlayer.release()

			init() // init sets lastPosition to 0, therefore we set the position after ini
			lastPosition = position
			postStateChangedEvent(true)
		}).letThis { this.enhancedHandler.postDelayed(it, RELEASE_DELAY) }

		this.exoPlayer.playWhenReady = false

		this.manager.removeSoundFromCurrentlyPlayingSounds(this)
		this.postStateChangedEvent(true)
		this.progressMonitor.stopProgressUpdateTimer()

		return !this.isPlayingSound
	}

	override fun fadeOutSound() {
		this.volumeController.fadeOutSound()
		this.postStateChangedEvent(true)
	}

	override fun setSoundUri(uri: String) {
		this.releasePlayerSchedule?.let { this.enhancedHandler.removeCallbacks(it) }

		this.mediaPlayerData.uri = uri

		this.init()
		this.postStateChangedEvent(true)
		this.progressMonitor.stopProgressUpdateTimer()
	}

	override fun destroy(postStateChanged: Boolean) {
		this.releasePlayerSchedule?.let { this.enhancedHandler.removeCallbacks(it) }
		this.volumeController.cancelFadeOut()

		this.exoPlayer.release()
		this.manager.removeSoundFromCurrentlyPlayingSounds(this)
		if (postStateChanged)
			this.postStateChangedEvent(false)

		this.progressMonitor.stopProgressUpdateTimer()
	}

	override fun onPlayerError(error: ExoPlaybackException) {
		Logger.e(TAG, "onPlayerError for $this with exception $error")
		this.init()
		this.eventBus.post(MediaPlayerFailedEvent(this, PlayerAction.UNDEFINED, error.message ?: ""))
		this.progressMonitor.stopProgressUpdateTimer()
	}

	override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
		Logger.d(TAG, "onPlayerStateChanged($playWhenReady, $playbackState)")
			if (playbackState == ExoPlayer.STATE_ENDED) {
			if (this.isLoopingEnabled) {
				this.progress = 0
				this.exoPlayer.playWhenReady = true
			}
			else {
				this.stopSound()
				this.eventBus.post(MediaPlayerCompletedEvent(this))
				this.progressMonitor.stopProgressUpdateTimer()
			}
		}

		this.enhancedHandler.postDelayed({ this.postStateChangedEvent(true) }, 100)
	}

	private fun postStateChangedEvent(isAlive: Boolean): Unit =
			this.eventBus.post(MediaPlayerStateChangedEvent(this, isAlive))

	override fun toString(): String =
		"ExoMediaPlayer(TAG='$TAG', " +
				"mediaPlayerData=$mediaPlayerData, " +
				"exoPlayer=$exoPlayer, lastPosition=$lastPosition, isDeletionPending=$isDeletionPending)"

	override fun onLoadingChanged(isLoading: Boolean) {}

	override fun onPositionDiscontinuity() {}

	override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}
}