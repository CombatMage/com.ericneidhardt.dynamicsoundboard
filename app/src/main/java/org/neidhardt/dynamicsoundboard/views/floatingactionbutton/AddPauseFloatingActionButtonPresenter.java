package org.neidhardt.dynamicsoundboard.views.floatingactionbutton;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

import java.util.Set;

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenter extends BaseViewPresenter<AddPauseFloatingActionButton> implements MediaPlayerEventListener
{
	private SoundsDataAccess soundsDataAccess;

	boolean isStatePause = false;

	public AddPauseFloatingActionButtonPresenter(SoundsDataAccess soundsDataAccess)
	{
		this.soundsDataAccess = soundsDataAccess;
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return true;
	}

	void onFabClicked()
	{
		this.getEventBus().post(new FabClickedEvent());
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.updateToMediaPlayersState();
	}

	@Override
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		this.updateToMediaPlayersState();
	}

	@Override
	public void onEvent(MediaPlayerCompletedEvent event)
	{
		this.updateToMediaPlayersState();
	}

	private void updateToMediaPlayersState()
	{
		Set<EnhancedMediaPlayer> currentlyPlayingSounds = this.soundsDataAccess.getCurrentlyPlayingSounds();
		if (currentlyPlayingSounds.size() > 0)
			this.setPauseState(true);
		else
			this.setPauseState(false);
	}

	private void setPauseState(boolean isStatePause)
	{
		if (this.isStatePause == isStatePause)
			return;

		this.isStatePause = isStatePause;

		AddPauseFloatingActionButton button = this.getView();
		if (button != null)
		{
			button.refreshDrawableState();
			button.animateUiChanges();
		}
	}

	public boolean isStatePause()
	{
		return isStatePause;
	}
}
