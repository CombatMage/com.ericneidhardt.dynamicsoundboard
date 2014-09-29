package com.ericneidhardt.dynamicsoundboard.misc;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class SoundProgressAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>
{

	public static final int UPDATE_INTERVAL = 500;
	public static final Handler handler = new Handler();

	protected List<EnhancedMediaPlayer> sounds;
	private Timer progressBarUpdateTimer;

	public SoundProgressAdapter()
	{
		this.sounds = new ArrayList<EnhancedMediaPlayer>();
	}

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
						List<Integer> itemsWithProgressChanged = getPlayingItems();
						if (itemsWithProgressChanged == null || itemsWithProgressChanged.size() == 0)
						{
							stopProgressUpdateTimer();
							return;
						}
						for (Integer index : itemsWithProgressChanged)
							notifyItemChanged(index);
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

	private List<Integer> getPlayingItems()
	{
		List<Integer> playingSounds = new ArrayList<Integer>();
		for (int i = 0; i < this.sounds.size(); i++)
		{
			if (this.sounds.get(i).isPlaying())
				playingSounds.add(i);
		}
		return playingSounds;
	}
}
