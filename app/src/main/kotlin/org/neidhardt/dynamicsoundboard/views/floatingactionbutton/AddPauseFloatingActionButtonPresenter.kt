package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent
import org.neidhardt.dynamicsoundboard.views.presenter.ViewPresenter

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenter
(
		override val eventBus: EventBus,
		private val soundsDataAccess: SoundsDataAccess
)
:
		ViewPresenter<AddPauseFloatingActionButton?>,
		MediaPlayerEventListener
{
	override val isEventBusSubscriber: Boolean = true
	override var view: AddPauseFloatingActionButton? = null

	var isStatePause = false

	fun onFabClicked()
	{
		this.eventBus.post(FabClickedEvent())
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.updateToMediaPlayersState()
	}

	override fun onEvent(event: MediaPlayerStateChangedEvent)
	{
		this.updateToMediaPlayersState()
	}

	override fun onEvent(event: MediaPlayerCompletedEvent)
	{
		this.updateToMediaPlayersState()
	}

	private fun updateToMediaPlayersState()
	{
		val currentlyPlayingSounds = this.soundsDataAccess.currentlyPlayingSounds
		if (currentlyPlayingSounds.size > 0)
			this.setPauseState(true)
		else
			this.setPauseState(false)
	}

	private fun setPauseState(isStatePause: Boolean)
	{
		if (this.isStatePause == isStatePause)
			return

		this.isStatePause = isStatePause

		val button = this.view
		if (button != null)
		{
			button.refreshDrawableState()
			button.animateUiChanges()
		}
	}
}