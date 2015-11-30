package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.views.presenter.ViewPresenter

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
	private val presenter = NavigationDrawerHeaderPresenter(EventBus.getDefault(), SoundboardApplication.getSoundLayoutsAccess())

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
		LayoutInflater.from(context).inflate(R.layout.navigation_drawer_header, this, true)
		this.currentLayoutName = this.findViewById(R.id.tv_current_sound_layout_name) as TextView
		this.indicator = this.findViewById(R.id.iv_change_sound_layout_indicator)

		this.findViewById(R.id.layout_change_sound_layout).setOnClickListener(this)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter.onAttachedToWindow()
	}

	override fun onDetachedFromWindow() {
		this.presenter.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter.view = this
	}

	override fun onClick(v: View) {
		this.presenter.onChangeLayoutClicked()
	}

	override fun showCurrentLayoutName(name: String) {
		this.currentLayoutName?.text = name
	}

	override fun animateLayoutChanges() {
		this.indicator?.animate()?.rotationXBy(180f)?.setDuration(this.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())?.start()
	}
}

class NavigationDrawerHeaderPresenter
(
		override val eventBus: EventBus,
		private val soundLayoutModel: SoundLayoutsAccess?
) :
		ViewPresenter<NavigationDrawerHeader?>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		OnSoundLayoutRemovedEventListener,
		OnSoundLayoutSelectedEventListener
{
	private val TAG = javaClass.name

	override val isEventBusSubscriber: Boolean = true

	override var view: NavigationDrawerHeader? = null

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
		this.eventBus.post(OpenSoundLayoutsRequestedEvent())
	}

	override fun onEvent(event: SoundLayoutRenamedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	override fun onEvent(event: SoundLayoutRemovedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	override fun onEvent(event: SoundLayoutSelectedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view?.animateLayoutChanges()
		this.view?.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}
}
