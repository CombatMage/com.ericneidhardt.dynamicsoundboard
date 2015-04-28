package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class ProgressbarHandler
{
	private ProgressBar progressBar;
	private int pendingEventCounter;

	public ProgressbarHandler(ProgressBar progressBar)
	{
		this.progressBar = progressBar;
		this.pendingEventCounter = 0;
	}

	public void showProgressBar(boolean showProgressBar)
	{
		if (showProgressBar)
		{
			this.progressBar.setVisibility(View.VISIBLE);
		}
		else
		{
			this.progressBar.setVisibility(View.GONE);
		}
	}

	public void onEvent(LongTermTaskEvent event)
	{
		if (event.isTaskFinished())
		{
			this.pendingEventCounter++;
			this.showProgressBar(true);
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
