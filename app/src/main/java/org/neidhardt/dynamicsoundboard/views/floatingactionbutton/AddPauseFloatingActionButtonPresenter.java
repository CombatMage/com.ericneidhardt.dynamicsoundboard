package org.neidhardt.dynamicsoundboard.views.floatingactionbutton;

import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenter extends BaseViewPresenter<AddPauseFloatingActionButton>
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
		// TODO get current state from soundsData and update UI
	}

	// TODO handle MediaPlayerStateChanged Events and update UI

	/**
	 * This is called by greenRobot EventBus in case the state of sounds in this activity has changed
	 * @param event delivered ActivitySoundsStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(ActivitySoundsStateChangedEvent event)
	{
		this.changeState(event.isAnySoundPlaying());
	}

	private void changeState(boolean isStatePause)
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
