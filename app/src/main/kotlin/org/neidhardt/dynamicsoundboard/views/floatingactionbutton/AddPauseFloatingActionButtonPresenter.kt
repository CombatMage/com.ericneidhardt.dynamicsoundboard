package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenter(private val soundsDataAccess: SoundsDataAccess)
:
		BaseViewPresenter<AddPauseFloatingActionButton>(),
		MediaPlayerEventListener
{
	public var isStatePause = false

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

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
		if (currentlyPlayingSounds.size() > 0)
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
