package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * File created by eric.neidhardt on 23.02.2016.
 */
open class VerboseMediaPlayer : MediaPlayer(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
	enum class State
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

	var currentState: State = State.IDLE

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
		this.currentState = State.PREPARED
	}

	override fun setDataSource(context: Context?, uri: Uri?)
	{
		super.setDataSource(context, uri)
		this.currentState = State.INIT
	}

	override fun start()
	{
		super.start()
		this.currentState = State.STARTED
	}

	override fun pause()
	{
		super.pause()
		this.currentState = State.PAUSED
	}

	override fun stop()
	{
		super.stop()
		this.currentState = State.STOPPED
	}

	override fun release()
	{
		super.release()
		this.currentState = State.DESTROYED
	}

	override fun onPrepared(mp: MediaPlayer?)
	{
		this.currentState = State.PREPARED
		this.onPreparedCallback?.onPrepared(mp)
	}

	override fun onCompletion(mp: MediaPlayer?) {
		if (!this.isLooping)
		{
			this.currentState = State.COMPLETED
			this.onComletionCallback?.onCompletion(mp)
		}
	}

	override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean
	{
		this.currentState = State.ERROR
		return this.onErrorCallback?.onError(mp, what, extra) ?: false
	}
}
