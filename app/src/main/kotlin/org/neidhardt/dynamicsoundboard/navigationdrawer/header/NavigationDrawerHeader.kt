package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_navigation_drawer_header.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess

/**
 * File created by eric.neidhardt on 12.11.2015.
 */
class NavigationDrawerHeaderView :
		FrameLayout,
		OnSoundLayoutRenamedEventListener,
		OnSoundLayoutsChangedEventListener,
		OnSoundLayoutSelectedEventListener
{
	private val eventBus = EventBus.getDefault()
	private val soundLayoutModel: SoundLayoutsAccess by lazy {
		SoundboardApplication.soundLayoutsAccess
	}
	private val presenter: NavigationDrawerHeaderPresenter by lazy {
		NavigationDrawerHeaderPresenter()
	}

	private var openSoundLayouts = true

	@SuppressWarnings("unused")
	constructor(context: Context) : super(context) {
		this.init(context)
	}

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.init(context)
	}

	@SuppressWarnings("unused")
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.init(context)
	}

	private fun init(context: Context) {
		LayoutInflater.from(context).inflate(R.layout.view_navigation_drawer_header, this, true)
	}

	override fun onFinishInflate() {
		super.onFinishInflate()

		this.rl_view_navigation_drawer_header_change_sound_layout.setOnClickListener {
			this.presenter.animateLayoutChanges()
			this.eventBus.post(OpenSoundLayoutsRequestedEvent(openSoundLayouts))
			openSoundLayouts = !openSoundLayouts
		}

		this.presenter.init(
				titleView = this.tv_view_navigation_drawer_header_current_sound_layout_name,
				viewToAnimate = this.iv_view_navigation_drawer_header_change_sound_layout_indicator
		)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter.start()
		this.presenter.title = this.soundLayoutModel.currentTitle
	}

	override fun onDetachedFromWindow() {
		this.presenter.stop()
		super.onDetachedFromWindow()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutRenamedEvent) {
		this.presenter.title = this.soundLayoutModel.currentTitle
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutsRemovedEvent) {
		this.presenter.title = this.soundLayoutModel.currentTitle
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent) {
		this.openSoundLayouts = true
		this.presenter.animateLayoutChanges()
		this.presenter.title = this.soundLayoutModel.currentTitle
	}
}

private val SoundLayoutsAccess.currentTitle: String
	get() = this.getActiveSoundLayout().label
