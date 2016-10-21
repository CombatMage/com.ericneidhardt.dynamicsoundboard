package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

import android.os.Handler
import android.support.v7.widget.RecyclerView
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.ui_utils.recyclerview.adapter.BaseAdapter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

val UPDATE_INTERVAL: Int = 500

abstract class SoundProgressAdapter<T>
(
		private val  recyclerView: RecyclerView
) :
		BaseAdapter<MediaPlayerController, T>(),
		SoundProgressTimer,
		Runnable
where T : RecyclerView.ViewHolder, T : SoundProgressViewHolder
{
	private val handler: Handler = Handler()
	private val hasTimerStarted: AtomicBoolean = AtomicBoolean(false)

	/**
	 * Starts periodic updates of sounds loaded in the adapter. This is used to update the progress bars of running sounds.
	 */
	override fun startProgressUpdateTimer()
	{
		if (!this.hasTimerStarted.getAndSet(true))
			this.handler.postDelayed(this, UPDATE_INTERVAL.toLong())
	}

	override fun stopProgressUpdateTimer()
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
			val viewHolderToUpdate = this.recyclerView.findViewHolderForAdapterPosition(index) as SoundProgressViewHolder?
			viewHolderToUpdate?.onProgressUpdate()
		}
		this.hasTimerStarted.set(false)
		this.startProgressUpdateTimer()
	}

	private fun getPlayingItems(): List<Int>
	{
		val playingSounds = ArrayList<Int>()
		val allSounds = this.values
		val count = allSounds.size
		for (i in 0..count - 1)
		{
			if (allSounds[i].isPlayingSound)
				playingSounds.add(i)
		}
		return playingSounds
	}

}
