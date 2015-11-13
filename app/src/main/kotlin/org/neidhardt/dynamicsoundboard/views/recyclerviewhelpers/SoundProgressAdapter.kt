package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

import android.os.Handler
import android.support.v7.widget.RecyclerView
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

public val UPDATE_INTERVAL: Int = 500

public abstract class SoundProgressAdapter<T : RecyclerView.ViewHolder> :
		BaseAdapter<MediaPlayerController, T>(),
		SoundProgressTimer,
		Runnable where T : SoundProgressViewHolder
{

	private val TAG = javaClass.name

	private val handler: Handler = Handler()
	private val hasTimerStarted: AtomicBoolean = AtomicBoolean(false)

	var recyclerView: RecyclerView? = null

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	public override fun startProgressUpdateTimer()
	{
		if (!this.hasTimerStarted.getAndSet(true))
			this.handler.postDelayed(this, UPDATE_INTERVAL.toLong())
	}

	public override fun stopProgressUpdateTimer()
	{
		if (this.hasTimerStarted.getAndSet(false))
			this.handler.removeCallbacks(this)
	}

	override fun run()
	{
		val itemsWithProgressChanged = getPlayingItems()
		if (itemsWithProgressChanged.size == 0)
		{
			this.stopProgressUpdateTimer()
			return
		}
		for (index in itemsWithProgressChanged)
		{
			if (this.recyclerView == null)
				throw NullPointerException(TAG + ": update sound progress failed, RecyclerView is null")

			val viewHolderToUpdate = this.recyclerView?.findViewHolderForAdapterPosition(index) as SoundProgressViewHolder?
			viewHolderToUpdate?.onProgressUpdate()
		}
		this.hasTimerStarted.set(false)
		this.startProgressUpdateTimer()
	}

	private fun getPlayingItems(): List<Int>
	{
		val playingSounds = ArrayList<Int>()
		val allSounds = this.getValues()
		val count = allSounds.size
		for (i in 0..count - 1)
		{
			if (allSounds[i].isPlayingSound)
				playingSounds.add(i)
		}
		return playingSounds
	}

}
