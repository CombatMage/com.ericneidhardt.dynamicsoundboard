package com.ericneidhardt.dynamicsoundboard.misc;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class SoundProgressAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements Runnable
{
	protected static final int UPDATE_INTERVAL = 500;
	protected final Handler handler = new Handler();

	protected ServiceManagerFragment serviceManagerFragment;

	public void setServiceManagerFragment(ServiceManagerFragment serviceManagerFragment)
	{
		this.serviceManagerFragment = serviceManagerFragment;
	}

	public void onParentPause()
	{
		this.stopProgressUpdateTimer();
		this.handler.removeCallbacks(null);
	}

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	public void scheduleProgressUpdateTimer()
	{
		this.handler.postDelayed(this, UPDATE_INTERVAL);
	}

	@Override
	public void run()
	{
		List<Integer> itemsWithProgressChanged = getPlayingItems();
		if (itemsWithProgressChanged == null || itemsWithProgressChanged.size() == 0)
		{
			stopProgressUpdateTimer();
			return;
		}
		for (Integer index : itemsWithProgressChanged)
			notifyItemChanged(index);

		this.scheduleProgressUpdateTimer();
	}

	public void stopProgressUpdateTimer()
	{
		handler.removeCallbacks(this);
	}

	private List<Integer> getPlayingItems()
	{
		List<Integer> playingSounds = new ArrayList<>();
		for (int i = 0; i < this.getValues().size(); i++)
		{
			if (this.getValues().get(i).isPlaying())
				playingSounds.add(i);
		}
		return playingSounds;
	}

	protected abstract List<EnhancedMediaPlayer> getValues();
}
