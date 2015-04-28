package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.view.View;
import de.greenrobot.event.EventBus;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LongTermTaskStartedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LongTermTaskStoppedEvent;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class ProgressbarHandler
{
	private static final String TAG = ProgressbarHandler.class.getName();

	private SmoothProgressBar progressBar;
	private int pendingEventCounter;

	public ProgressbarHandler(SmoothProgressBar progressBar)
	{
		this.progressBar = progressBar;
		this.progressBar.setVisibility(View.GONE);
		this.pendingEventCounter = 0;
	}

	public void showProgressBar(boolean showProgressBar)
	{
		Logger.d(TAG, "showProgressBar() " + showProgressBar);
		if (showProgressBar)
		{
			this.progressBar.setVisibility(View.VISIBLE);
		}
		else
		{
			this.progressBar.setVisibility(View.GONE);
		}
	}

	public void onEvent(LongTermTaskStartedEvent event)
	{
		Logger.d(TAG, "onEvent() " + event);
		this.pendingEventCounter++;
		this.showProgressBar(true);
	}

	public void onEvent(LongTermTaskStoppedEvent event)
	{
		Logger.d(TAG, "onEvent() " + event);
		EventBus.getDefault().removeStickyEvent(event);

		if (this.pendingEventCounter > 0)
			this.pendingEventCounter--;
		if (this.pendingEventCounter == 0)
			this.showProgressBar(false);
	}

	public int getPendingEventCounter()
	{
		return pendingEventCounter;
	}
}
