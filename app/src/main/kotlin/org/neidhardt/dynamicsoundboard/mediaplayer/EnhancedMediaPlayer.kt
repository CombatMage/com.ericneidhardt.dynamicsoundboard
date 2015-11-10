package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess

import java.io.IOException

private enum class State
{
	IDLE,
	INIT,
	PREPARED,
	STARTED,
	STOPPED,
	PAUSED,
	DESTROYED
}

class EnhancedMediaPlayer
(
		context: Context,
		private val eventBus: EventBus,
		public override val mediaPlayerData: MediaPlayerData,
		private val soundsDataAccess: SoundsDataAccess
) :
		MediaPlayer(),
		MediaPlayerController,
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener,
		MediaPlayer.OnInfoListener,
		Runnable
{

	private var currentState: State? = null
	private var duration: Int = 0
	private var volume: Int = 0

	private var handler: Handler? = null

	init
	{
		this.setOnErrorListener(this)
		this.setOnInfoListener(this)

		this.isLooping = mediaPlayerData.isLoop

		this.currentState = State.IDLE
		this.init(context)
	}

	@Throws(IOException::class)
	override fun setSoundUri(uri: String)
	{
		this.mediaPlayerData.uri = uri
		this.mediaPlayerData.updateItemInDatabaseAsync()
		this.reset()

		this.init(DynamicSoundboardApplication.getContext())
	}

	@Throws(IOException::class)
	private fun init(context: Context)
	{
		if (this.mediaPlayerData.uri == null)
			throw NullPointerException("cannot initIfRequired media player, sound uri is null")

		this.setAudioStreamType(AudioManager.STREAM_MUSIC)
		val soundUri = Uri.parse(this.mediaPlayerData.uri)
		this.setDataSource(context, soundUri)
		this.isLooping = this.mediaPlayerData.isLoop
		this.prepare()
		this.currentState = State.PREPARED

		this.volume = INT_VOLUME_MAX
		this.duration = super.getDuration()
		this.setOnCompletionListener(this)
	}

	override fun destroy(postStateChanged: Boolean)
	{
		if (this.handler != null)
			this.handler!!.removeCallbacks(this)
		this.currentState = State.DESTROYED
		this.reset()
		this.release()
		this.soundsDataAccess.currentlyPlayingSounds.remove(this)
		if (postStateChanged)
			this.postStateChangedEvent(false)
	}

	@Throws(IOException::class, IllegalStateException::class)
	override fun prepare()
	{
		Logger.d(TAG, "preparing media player " + this.mediaPlayerData.label + " with uri " + this.mediaPlayerData.uri)
		super.prepare()
	}

	override fun getDuration(): Int
	{
		if (this.currentState == State.DESTROYED || this.currentState == State.IDLE)
			return 0
		return this.duration
	}

	/**
	 * Check if this MediaPlayer is currently playing, ie. State.STARTED.
	 * The call is not forwarded to the native implementation (super.isPlaying), because
	 * of ab described here:
	 * @see [.9732: internal/external state mismatch corrected](https://code.google.com/p/android/issues/detail?id=9732)

	 * @return true if player ist playing, false otherwise
	 */
	override fun isPlaying(): Boolean
	{
		return this.currentState == State.STARTED
	}

	override fun getCurrentPosition(): Int
	{
		if (this.currentState == State.DESTROYED)
			return 0
		return super.getCurrentPosition()
	}

	override fun setLooping(looping: Boolean)
	{
		super.setLooping(looping)

		if (this.mediaPlayerData.isLoop != looping)
		{
			this.mediaPlayerData.isLoop = looping
			this.mediaPlayerData.updateItemInDatabaseAsync()
		}
	}

	override fun setIsInPlaylist(inPlaylist: Boolean)
	{
		this.mediaPlayerData.isInPlaylist = inPlaylist
		this.mediaPlayerData.updateItemInDatabaseAsync()
	}

	override fun playSound(): Boolean
	{
		if (this.isPlaying)
			return true
		try
		{
			if (this.currentState == State.IDLE || this.currentState == State.DESTROYED)
				this.init(DynamicSoundboardApplication.getContext())

			if (this.currentState == State.INIT || this.currentState == State.STOPPED)
				this.prepare()

			this.volume = INT_VOLUME_MAX
			this.updateVolume(this.volume)

			this.start()
			this.currentState = State.STARTED

			this.soundsDataAccess.currentlyPlayingSounds.add(this)
			this.postStateChangedEvent(true)
			return true
		}
		catch (e: IOException)
		{
			Logger.e(TAG, e.toString())
			this.reportExceptions(e)
			return false
		}
		catch (e: IllegalStateException)
		{
			Logger.e(TAG, e.toString())
			this.reportExceptions(e)
			return false
		}

	}

	override fun stopSound(): Boolean
	{
		if (this.pauseSound())
		{
			this.seekTo(0)
			return true
		}
		return false
	}

	override fun pauseSound(): Boolean {
		if (!this.isPlaying)
			return true
		try {
			when (this.currentState)
			{
				State.IDLE -> {
					this.init(DynamicSoundboardApplication.getContext())
					this.start()
				}
				State.DESTROYED -> {
					this.init(DynamicSoundboardApplication.getContext())
					this.start()
				}
				State.INIT -> {
					this.prepare()
					this.start()
				}
				State.PREPARED -> this.start()
				State.STARTED -> {
				}
				State.PAUSED -> {
				}
				State.STOPPED -> {
					this.prepare()
					this.start()
				}
				else -> return false // should not be reached
			}
			this.pause()
			this.currentState = State.PAUSED

			this.soundsDataAccess.currentlyPlayingSounds.remove(this)
			this.postStateChangedEvent(true)
			return true
		}
		catch (e: IOException)
		{
			Logger.e(TAG, e.getMessage())
			this.reportExceptions(e)
			return false
		}
		catch (e: IllegalStateException)
		{
			Logger.e(TAG, e.getMessage())
			this.reportExceptions(e)
			return false
		}
	}

	override fun fadeOutSound()
	{
		this.updateVolume(0)
		this.scheduleNextVolumeChange()
	}

	private fun scheduleNextVolumeChange()
	{
		val delay = FADE_OUT_DURATION / INT_VOLUME_MAX
		if (this.handler == null)
			this.handler = Handler()
		this.handler!!.postDelayed(this, delay.toLong())
	}

	override fun run()
	{
		updateVolume(-1)
		if (volume == INT_VOLUME_MIN) {
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

	override fun setPositionTo(timePosition: Int): Boolean
	{
		try
		{
			when (this.currentState)
			{
				State.INIT -> {
					this.prepare()
					this.seekTo(timePosition)
					this.currentState = State.PREPARED
					return true
				}
				State.PREPARED -> {
					this.seekTo(timePosition)
					return true
				}
				State.STARTED -> {
					this.seekTo(timePosition)
					return true
				}
				State.PAUSED -> {
					this.seekTo(timePosition)
					return true
				}
				State.STOPPED -> {
					this.prepare()
					this.seekTo(timePosition)
					this.currentState = State.PREPARED
					return true
				}
				else -> return false
			}
		}
		catch (e: IOException)
		{
			Logger.e(TAG, e.getMessage())
			this.reportExceptions(e)
			return false
		}
		catch (e: IllegalStateException)
		{
			Logger.e(TAG, e.getMessage())
			this.reportExceptions(e)
			return false
		}

	}

	private fun reportExceptions(e: Exception)
	{
		DynamicSoundboardApplication.reportError(e)
	}

	override fun onCompletion(mp: MediaPlayer)
	{
		// for unknown reason, onCompletion is called even if the player is set to looping, therefore we needs to do an additional check
		if (this.isLooping)
			return

		// for unknown reason, this must be set to paused instead of stopped. This contradicts MediaPlayer Documentation, but calling prepare for restart throws illegal state exception
		this.currentState = State.PAUSED
		this.soundsDataAccess.currentlyPlayingSounds.remove(this)
		this.postStateChangedEvent(true)
		this.postCompletedEvent()
	}

	private fun postStateChangedEvent(isAlive: Boolean)
	{
		this.eventBus.post(MediaPlayerStateChangedEvent(this, isAlive))
	}

	private fun postCompletedEvent()
	{
		this.eventBus.post(MediaPlayerCompletedEvent(this))
	}

	override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean
	{
		Logger.e(TAG, "onError(" + mp.toString() + ") what: " + what + " extra: " + extra)

		return false
	}

	override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean
	{
		Logger.d(TAG, "onError(" + mp.toString() + ") what: " + what + " extra: " + extra)

		return false
	}

	override fun toString(): String {
		return "EnhancedMediaPlayer{currentState=$currentState, duration=$duration, volume=$volume, handler=$handler, soundsDataAccess=$soundsDataAccess, rawData=$mediaPlayerData}"
	}

	companion object
	{
		private val TAG = EnhancedMediaPlayer::class.java.name

		private val FADE_OUT_DURATION = 100
		private val INT_VOLUME_MAX = 100
		private val INT_VOLUME_MIN = 0
		private val FLOAT_VOLUME_MAX = 1f
		private val FLOAT_VOLUME_MIN = 0f

		@Throws(IOException::class)
		fun getInstanceForPlayList(data: MediaPlayerData): EnhancedMediaPlayer
		{
			val playListData = MediaPlayerData()
			playListData.id = data.id
			playListData.isInPlaylist = true
			playListData.playerId = data.playerId
			playListData.fragmentTag = Playlist.TAG
			playListData.isLoop = false
			playListData.label = data.label
			playListData.uri = data.uri

			return EnhancedMediaPlayer (
					context = DynamicSoundboardApplication.getContext(),
					eventBus = EventBus.getDefault(),
					mediaPlayerData = playListData,
					soundsDataAccess = DynamicSoundboardApplication.getSoundsDataAccess()
			)
		}

		fun getMediaPlayerData(fragmentTag: String, uri: Uri, label: String): MediaPlayerData
		{
			val data = MediaPlayerData()

			val playerId = Integer.toString((uri.toString() + DynamicSoundboardApplication.getRandomNumber()).hashCode())
			data.playerId = playerId
			data.fragmentTag = fragmentTag
			data.label = label
			data.uri = uri.toString()
			data.isInPlaylist = false
			data.isLoop = false

			return data
		}
	}
}
