package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * File created by eric.neidhardt on 23.02.2016.
 */
enum class MediaPlayerState
{
	IDLE,
	INIT,
	PREPARED,
	STARTED,
	STOPPED,
	PAUSED,
	COMPLETED,
	DESTROYED,
	ERROR
}

class VerboseMediaPlayer : MediaPlayer(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{

	var currentState: MediaPlayerState = MediaPlayerState.IDLE

	private var onPreparedCallback: OnPreparedListener? = null
	private var onErrorCallback: OnErrorListener? = null
	private var onComletionCallback: OnCompletionListener? = null

	init
	{
		super.setOnPreparedListener(this)
		super.setOnErrorListener(this)
		super.setOnCompletionListener(this)
	}

	override fun setOnPreparedListener(listener: OnPreparedListener?)
	{
		this.onPreparedCallback = listener
	}

	override fun setOnErrorListener(listener: OnErrorListener?)
	{
		this.onErrorCallback = listener
	}

	override fun setOnCompletionListener(listener: OnCompletionListener?)
	{
		this.onComletionCallback = listener
	}

	override fun prepare()
	{
		super.prepare()
		this.currentState = MediaPlayerState.PREPARED
	}

	override fun setDataSource(context: Context?, uri: Uri?)
	{
		super.setDataSource(context, uri)
		this.currentState = MediaPlayerState.INIT
	}

	override fun start()
	{
		super.start()
		this.currentState = MediaPlayerState.STARTED
	}

	override fun pause()
	{
		super.pause()
		this.currentState = MediaPlayerState.PAUSED
	}

	override fun stop()
	{
		super.stop()
		this.currentState = MediaPlayerState.STOPPED
	}

	override fun release()
	{
		super.release()
		this.currentState = MediaPlayerState.DESTROYED
	}

	override fun onPrepared(mp: MediaPlayer?)
	{
		this.currentState = MediaPlayerState.PREPARED
		this.onPreparedCallback?.onPrepared(mp)
	}

	override fun onCompletion(mp: MediaPlayer?) {
		if (!this.isLooping)
		{
			this.currentState = MediaPlayerState.COMPLETED
			this.onComletionCallback?.onCompletion(mp)
		}
	}

	override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean
	{
		this.currentState = MediaPlayerState.ERROR
		return this.onErrorCallback?.onError(mp, what, extra) ?: false
	}
}
