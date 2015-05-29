package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class SoundProgressAdapter<T extends RecyclerView.ViewHolder & SoundProgressViewHolder>
		extends
			RecyclerView.Adapter<T>
		implements
			Runnable
{
	private static final String TAG = SoundProgressAdapter.class.getName();

	public static final int UPDATE_INTERVAL = 500;

	protected final Handler handler = new Handler();

	protected ServiceManagerFragment serviceManagerFragment;

	private RecyclerView recyclerView;

	public void setServiceManagerFragment(ServiceManagerFragment serviceManagerFragment)
	{
		this.serviceManagerFragment = serviceManagerFragment;
	}

	public void setRecyclerView(RecyclerView recyclerView)
	{
		this.recyclerView = recyclerView;
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
		{
			if (this.recyclerView == null)
				throw new NullPointerException(TAG + ": update sound progress failed, RecyclerView is null");

			SoundProgressViewHolder viewHolderToUpdate = (SoundProgressViewHolder) this.recyclerView.findViewHolderForAdapterPosition(index);
			if (viewHolderToUpdate != null)
				viewHolderToUpdate.onProgressUpdate();
		}
		this.startProgressUpdateTimer();
	}

	private List<Integer> getPlayingItems()
	{
		List<Integer> playingSounds = new ArrayList<>();
		List<EnhancedMediaPlayer> allSounds = this.getValues();
		int count = allSounds != null ? allSounds.size() : 0;
		for (int i = 0; i < count; i++)
		{
			if (allSounds.get(i).isPlaying())
				playingSounds.add(i);
		}
		return playingSounds;
	}

	protected abstract List<EnhancedMediaPlayer> getValues();

}
