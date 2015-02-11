package org.neidhardt.dynamicsoundboard.misc;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

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

	public void onParentResume()
	{
		EventBus.getDefault().register(this);
	}

	public void onParentPause()
	{
		EventBus.getDefault().unregister(this);
		this.stopProgressUpdateTimer();
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		// TODO is this onResume?
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		super.onDetachedFromRecyclerView(recyclerView);
		// TODO is this onPause?
	}

	/**
	 * This is called by greenDao EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		String playerId = event.getPlayerId();
		List<EnhancedMediaPlayer> players = this.getValues();
		int count = players.size();
		for (int i = 0; i < count; i++)
		{
			if (players.get(i).getMediaPlayerData().getPlayerId().equals(playerId))
				this.notifyItemChanged(i);
		}
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
		this.handler.removeMessages(0);
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
