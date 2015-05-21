package org.neidhardt.dynamicsoundboard.customview.floatingactionbutton;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.customview.BasePresenter;
import org.neidhardt.dynamicsoundboard.events.FabClickedEvent;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenter extends BasePresenter<AddPauseFloatingActionButton>
{
	EventBus bus;

	public AddPauseFloatingActionButtonPresenter()
	{
		this.bus = EventBus.getDefault();
	}

	public void onFabClicked()
	{
		this.bus.post(new FabClickedEvent());
	}
}
