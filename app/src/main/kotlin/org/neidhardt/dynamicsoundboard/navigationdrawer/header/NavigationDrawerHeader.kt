package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_navigation_drawer_header.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import org.neidhardt.ui_utils.presenter.ViewPresenterOld

/**
 * File created by eric.neidhardt on 12.11.2015.
 */
interface NavigationDrawerHeader
{
	fun showCurrentLayoutName(name: String)

	fun animateLayoutChanges()
}

class NavigationDrawerHeaderView :
		FrameLayout,
		View.OnClickListener,
		NavigationDrawerHeader
{
	private var presenter: NavigationDrawerHeaderPresenter? = null

	private var currentLayoutName: TextView? = null
	private var indicator: View? = null

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
		this.currentLayoutName = this.tv_view_navigation_drawer_header_current_sound_layout_name
		this.indicator = this.iv_view_navigation_drawer_header_change_sound_layout_indicator

		this.rl_view_navigation_drawer_header_change_sound_layout.setOnClickListener(this)

		if (!this.isInEditMode)
			this.presenter = NavigationDrawerHeaderPresenter(EventBus.getDefault(), SoundboardApplication.soundLayoutsAccess)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter?.onAttachedToWindow()
	}

	override fun onDetachedFromWindow() {
		this.presenter?.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter?.view = this
	}

	override fun onClick(v: View) {
		this.presenter?.onChangeLayoutClicked()
	}

	override fun showCurrentLayoutName(name: String) {
		this.currentLayoutName?.text = name
	}

	override fun animateLayoutChanges() {
		this.indicator?.apply {
			this.animate().withLayer()
					.rotationXBy(180f)
					.setDuration(this.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
					.start()
		}
	}
}

class NavigationDrawerHeaderPresenter
(
		override val eventBus: EventBus,
		private val soundLayoutModel: SoundLayoutsAccess?
) :
		ViewPresenterOld<NavigationDrawerHeader?>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
        OnSoundLayoutsChangedEventListener,
        OnSoundLayoutSelectedEventListener
{
	private val TAG = javaClass.name

	override val isEventBusSubscriber: Boolean = true

	override var view: NavigationDrawerHeader? = null

	private var openSoundLayouts = true

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (this.view == null)
			throw NullPointerException("$TAG: supplied view is null")

		if (this.soundLayoutModel == null)
			throw NullPointerException("$TAG: supplied model is null")

		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	fun onChangeLayoutClicked() {
		this.view?.animateLayoutChanges()
		this.eventBus.post(OpenSoundLayoutsRequestedEvent(openSoundLayouts))
		openSoundLayouts = !openSoundLayouts
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutRenamedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutsRemovedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.openSoundLayouts = true
		this.view?.animateLayoutChanges()
		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}
}
