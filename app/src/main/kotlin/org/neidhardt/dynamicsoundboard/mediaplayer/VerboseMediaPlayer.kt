package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * File created by eric.neidhardt on 23.02.2016.
 */
class VerboseMediaPlayer : MediaPlayer(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
{
	enum class State
	{
		IDLE,
		INIT,
		PREPARED,
		STARTED,
		STOPPED,
		PAUSED,
		DESTROYED,
		ERROR
	}

	var currentState: State = State.IDLE

	private var onPreparedCallback: OnPreparedListener? = null
	private var onErrorCallback: OnErrorListener? = null

	init {
		super.setOnPreparedListener(this)
		super.setOnErrorListener(this)
	}

	override fun setOnPreparedListener(listener: OnPreparedListener?) {
		this.onPreparedCallback = listener
	}

	override fun prepare() {
		super.prepare()
		this.currentState = State.PREPARED
	}

	override fun onPrepared(mp: MediaPlayer?) {
		this.currentState = State.PREPARED
		this.onPreparedCallback?.onPrepared(this)
	}

	override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
		this.currentState = State.ERROR
		return this.onErrorCallback?.onError(p0, p1, p2) ?: false
	}

	override fun setDataSource(context: Context?, uri: Uri?) {
		super.setDataSource(context, uri)
		this.currentState = State.INIT
	}

	override fun start() {
		super.start()
		this.currentState = State.STARTED
	}

	override fun pause() {
		super.pause()
		this.currentState = State.PAUSED
	}

	override fun stop() {
		super.stop()
		this.currentState = State.STOPPED
	}

	override fun release() {
		super.release()
		this.currentState = State.DESTROYED
	}
}
