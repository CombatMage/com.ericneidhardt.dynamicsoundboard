package org.neidhardt.dynamicsoundboard.misc;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class SoundProgressAdapter<T extends RecyclerView.ViewHolder>
		extends
			RecyclerView.Adapter<T>
		implements
			Runnable
{
	public static final int UPDATE_INTERVAL = 500;
	protected final Handler handler = new Handler();

	protected ServiceManagerFragment serviceManagerFragment;

	public void setServiceManagerFragment(ServiceManagerFragment serviceManagerFragment)
	{
		this.serviceManagerFragment = serviceManagerFragment;
	}

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	public void startProgressUpdateTimer()
	{
		this.handler.postDelayed(this, UPDATE_INTERVAL);
	}

	public void stopProgressUpdateTimer()
	{
		this.handler.removeMessages(0);
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

		this.startProgressUpdateTimer();
	}

	private List<Integer> getPlayingItems()
	{
		List<Integer> playingSounds = new ArrayList<>();
		List<EnhancedMediaPlayer> allSounds = this.getValues();
		int count = allSounds.size();
		for (int i = 0; i < count; i++)
		{
			if (allSounds.get(i).isPlaying())
				playingSounds.add(i);
		}
		return playingSounds;
	}

	protected abstract List<EnhancedMediaPlayer> getValues();
}
