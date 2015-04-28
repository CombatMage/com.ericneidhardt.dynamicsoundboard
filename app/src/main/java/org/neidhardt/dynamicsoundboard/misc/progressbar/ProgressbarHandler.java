package org.neidhardt.dynamicsoundboard.misc.progressbar;

import de.greenrobot.event.EventBus;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import org.neidhardt.dynamicsoundboard.misc.Logger;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class ProgressbarHandler
{
	private static final String TAG = ProgressbarHandler.class.getName();

	private SmoothProgressBar progressBar;
	private int pendingEventCounter;
	private boolean isActive;

	public ProgressbarHandler(SmoothProgressBar progressBar)
	{
		this.progressBar = progressBar;
		this.pendingEventCounter = 0;
		this.isActive = false;
	}

	public void showProgressBar(boolean showProgressBar)
	{
		Logger.d(TAG, "showProgressBar() " + showProgressBar);
		if (showProgressBar && !this.isActive)
		{
			this.progressBar.progressiveStart();
			this.isActive = true;
		}
		else if (this.isActive)
		{
			this.progressBar.progressiveStop();
			this.isActive = false;
		}
	}

	public void onEvent(LongTermTaskEvent event)
	{
		Logger.d(TAG, "onEvent() " + event);
		if (event.isTaskStarted())
		{
			this.pendingEventCounter++;
			this.showProgressBar(true);
			EventBus.getDefault().removeStickyEvent(event);
		}
		else
		{
			if (this.pendingEventCounter > 0)
				this.pendingEventCounter--;
			if (this.pendingEventCounter == 0)
				this.showProgressBar(false);
		}
	}

	public int getPendingEventCounter()
	{
		return pendingEventCounter;
	}
}
