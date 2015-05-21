package org.neidhardt.dynamicsoundboard.customview.floatingactionbutton;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.customview.BaseViewPresenter;
import org.neidhardt.dynamicsoundboard.customview.floatingactionbutton.events.FabClickedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonViewPresenter extends BaseViewPresenter<AddPauseFloatingActionButton>
{
	AddPauseFloatingActionButtonViewPresenter()
	{
		this.setBus(EventBus.getDefault());
	}

	void onFabClicked()
	{
		this.getBus().post(new FabClickedEvent());
	}

	/**
	 * This is called by greenRobot EventBus in case the state of sounds in this activity has changed
	 * @param event delivered ActivitySoundsStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(ActivitySoundsStateChangedEvent event)
	{
		AddPauseFloatingActionButton button = this.getView();
		if (button == null)
			return;
		if (event.isAnySoundPlaying())
			button.setPauseState();
		else
			button.setAddState();
	}

}
