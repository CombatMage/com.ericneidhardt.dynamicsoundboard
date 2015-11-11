package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views

import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.*
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
class NavigationDrawerHeaderPresenter(private val soundLayoutModel: SoundLayoutsAccess?) :
		BaseViewPresenter<NavigationDrawerHeader>(),
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		OnSoundLayoutRemovedEventListener,
		OnSoundLayoutSelectedEventListener
{
	private val TAG = javaClass.name

	override fun isEventBusSubscriber(): Boolean = true

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (this.view == null)
			throw NullPointerException(TAG + ": supplied view is null")

		if (this.soundLayoutModel == null)
			throw NullPointerException(TAG + ": supplied model is null")

		this.view.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	@SuppressWarnings("unused")
	override fun onEvent(event: SoundLayoutRenamedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	@SuppressWarnings("unused")
	override fun onEvent(event: SoundLayoutRemovedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	@SuppressWarnings("unused")
	override fun onEvent(event: SoundLayoutSelectedEvent) {
		if (this.view == null || this.soundLayoutModel == null)
			return

		this.view.animateLayoutChanges()
		this.view.showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().label)
	}

	fun onChangeLayoutClicked() {
		this.view.animateLayoutChanges()
		this.eventBus.post(OpenSoundLayoutsRequestedEvent())
	}
}
