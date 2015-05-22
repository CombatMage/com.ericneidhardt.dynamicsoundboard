package org.neidhardt.dynamicsoundboard.views.progressbar;

import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.events.LongTermTaskStateChangedEvent;
import org.neidhardt.dynamicsoundboard.views.BaseViewPresenter;

/**
 * Created by eric.neidhardt on 22.05.2015.
 */
public class ActivityProgressBarPresenter extends BaseViewPresenter<ActivityProgressBar>
{
	private static final String TAG = ActivityProgressBarPresenter.class.getName();

	ActivityProgressBarPresenter()
	{
		this.showProgressBar(false);
		this.setBus(EventBus.getDefault());
	}
	/**
	 * This is called by greenRobot EventBus in case a background task starts or finishes his execution
	 * @param event delivered LongTermTaskStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(LongTermTaskStateChangedEvent event)
	{
		Logger.d(TAG, "onEvent() " + event);
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
