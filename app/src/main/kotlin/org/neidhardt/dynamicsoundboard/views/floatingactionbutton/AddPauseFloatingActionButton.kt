package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionButton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent

/**
 * File created by Eric Neidhardt on 12.11.2014.
 */
private val PAUSE_STATE = intArrayOf(R.attr.state_pause)

class AddPauseFloatingActionButton : FloatingActionButton, MediaPlayerEventListener {

	private val eventBus = EventBus.getDefault()
	private val storage by lazy {
		SoundboardApplication.soundsDataAccess
	}
	private val presenter: AddPauseFloatingActionButtonPresenter by lazy {
		AddPauseFloatingActionButtonPresenter()
	}

	@SuppressWarnings("unused")
	constructor(context: Context) : super(context)

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.setOnClickListener { this.eventBus.post(FabClickedEvent()) }
		this.presenter.init(this)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter.start()
		this.eventBus.register(this)
		this.setPresenterState()
	}

	override fun onDetachedFromWindow() {
		this.presenter.stop()
		this.eventBus.unregister(this)
		super.onDetachedFromWindow()
	}

	override fun onCreateDrawableState(extraSpace: Int): IntArray {
		val state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.size)
		if (this.presenter.state == AddPauseFloatingActionView.State.PLAY)
			View.mergeDrawableStates(state, PAUSE_STATE)
		return state
	}

	fun animateUiChanges() {
		this.post {
			AnimationUtils.createCircularReveal(this,
					width,
					height,
					0f,
					(height * 2).toFloat())?.apply { start() }
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		this.setPresenterState()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		this.setPresenterState()
	}

	private fun setPresenterState() {
		this.presenter.state =
				if (this.storage.isAnySoundPlaying)
					AddPauseFloatingActionView.State.PLAY
				else
					AddPauseFloatingActionView.State.ADD
	}
}

private val SoundsDataAccess.isAnySoundPlaying: Boolean
	get() = this.currentlyPlayingSounds.size > 0