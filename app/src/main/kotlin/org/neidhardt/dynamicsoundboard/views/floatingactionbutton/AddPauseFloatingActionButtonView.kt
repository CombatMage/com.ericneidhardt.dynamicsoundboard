package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.animations.AnimationUtils
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent

/**
 * File created by Eric Neidhardt on 12.11.2014.
 */
private val PAUSE_STATE = intArrayOf(R.attr.state_pause)

class FabClickedEvent

class AddPauseFloatingActionButtonView : FloatingActionButton, MediaPlayerEventListener {

	private val eventBus = EventBus.getDefault()

	private var subscriptions = CompositeDisposable()
	private var manager: SoundLayoutManager? = null
	private var presenter: AddPauseFloatingActionButtonPresenter? = null

	@SuppressWarnings("unused")
	constructor(context: Context) : super(context) { this.init() }

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { this.init() }

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) { this.init() }

	private fun init() {
		if (!this.isInEditMode) {
			this.manager = SoundboardApplication.soundLayoutManager
			this.presenter = AddPauseFloatingActionButtonPresenter()
		}
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.setOnClickListener { this.eventBus.post(FabClickedEvent()) }
		this.presenter?.init(this)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.subscriptions = CompositeDisposable()

		this.presenter?.start()
		this.setPresenterState()
		this.subscriptions.add(RxNewSoundLayoutManager.changesPlayingSounds(this.manager!!)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.setPresenterState() }
		)
	}

	override fun onDetachedFromWindow() {
		this.presenter?.stop()
		this.subscriptions.dispose()
		super.onDetachedFromWindow()
	}

	override fun onCreateDrawableState(extraSpace: Int): IntArray {
		val state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.size)
		if (this.presenter?.state == AddPauseFloatingAction.State.PLAY)
			View.mergeDrawableStates(state, PAUSE_STATE)
		return state
	}

	fun animateUiChanges() {
		this.post {
			if (android.support.v4.view.ViewCompat.isAttachedToWindow(this)) {
				AnimationUtils.createCircularReveal(this,
						width,
						height,
						0f,
						(height * 2).toFloat())?.start()
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		this.setPresenterState()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerCompletedEvent) {
		this.setPresenterState()
	}

	private fun setPresenterState() {
		this.presenter?.state =
				if (this.manager?.isAnySoundPlaying == true)
					AddPauseFloatingAction.State.PLAY
				else
					AddPauseFloatingAction.State.ADD
	}

	interface FabEventListener {
		fun onFabClickedEvent(event: FabClickedEvent)
	}
}

private val SoundLayoutManager.isAnySoundPlaying: Boolean get() = this.currentlyPlayingSounds.isNotEmpty()