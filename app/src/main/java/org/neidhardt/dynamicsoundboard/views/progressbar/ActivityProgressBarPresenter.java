package org.neidhardt.dynamicsoundboard.views.progressbar;

import android.view.View;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.events.LongTermTaskStateChangedEvent;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;

/**
 * Created by eric.neidhardt on 22.05.2015.
 */
public class ActivityProgressBarPresenter extends BaseViewPresenter<ActivityProgressBar>
{
	private static final String TAG = ActivityProgressBarPresenter.class.getName();

	private LongTermTaskStateChangedEvent lastReceivedEvent;

	public ActivityProgressBarPresenter()
	{
		this.lastReceivedEvent = null;
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return true;
	}

	@Override
	public void setView(ActivityProgressBar view)
	{
		super.setView(view); // setView takes place after construction of presenter. Therefore we store the last received event and adjust view state accordingly
		if (this.lastReceivedEvent != null)
			this.onEventMainThread(this.lastReceivedEvent);
	}

	/**
	 * This is called by greenRobot EventBus in case a background task starts or finishes his execution
	 * @param event delivered LongTermTaskStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(LongTermTaskStateChangedEvent event)
	{
		Logger.d(TAG, "onEvent() " + event);
		this.lastReceivedEvent = event;
		int countOngingTasks = event.getNrOngoingTasks();

		if (countOngingTasks > 0)
			this.showProgressBar(true);
		else
			this.showProgressBar(false);
	}

	private void showProgressBar(boolean showProgressBar)
	{
		Logger.d(TAG, "showProgressBar() " + showProgressBar);
		ActivityProgressBar progressBar = this.getView();
		if (progressBar == null)
			return;

		if (showProgressBar)
			progressBar.setVisibility(View.VISIBLE);
		else
			progressBar.setVisibility(View.GONE);
	}
}
