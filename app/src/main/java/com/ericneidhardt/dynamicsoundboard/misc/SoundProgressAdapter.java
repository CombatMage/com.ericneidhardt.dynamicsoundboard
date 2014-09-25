package com.ericneidhardt.dynamicsoundboard.misc;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;

import java.util.Timer;
import java.util.TimerTask;

public abstract class SoundProgressAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>
{

	public static final int UPDATE_INTERVAL = 500;
	public static final Handler handler = new Handler();

	private Timer progressBarUpdateTimer;

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	public void startProgressUpdateTimer()
	{
		TimerTask updateTimePositions = new TimerTask()
		{
			@Override
			public void run()
			{
				handler.post(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		};
		this.progressBarUpdateTimer = new Timer();
		this.progressBarUpdateTimer.schedule(updateTimePositions, 0, UPDATE_INTERVAL);
	}

	public void stopProgressUpdateTimer()
	{
		this.progressBarUpdateTimer.cancel();
	}
}
