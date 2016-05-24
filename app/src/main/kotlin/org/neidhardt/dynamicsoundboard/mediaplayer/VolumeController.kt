package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable

/**
 * File created by eric.neidhardt on 11.04.2016.
 */
private val FADE_OUT_DURATION = 100.toLong()
private val INT_VOLUME_MAX = 100
private val INT_VOLUME_MIN = 0
private val FLOAT_VOLUME_MAX = 1f
private val FLOAT_VOLUME_MIN = 0f

class VolumeController(private val mediaPlayerController: MediaPlayerController)
{
	private var volume: Int = INT_VOLUME_MAX
	private var fadeOutSchedule: KillableRunnable? = null

	private val handler = EnhancedHandler()

	val maxVolume = FLOAT_VOLUME_MAX

	fun fadeOutSound()
	{
		this.cancelFadeOut()
		this.updateVolume(0)
		this.scheduleNextVolumeChange()
	}

	fun cancelFadeOut()
	{
		this.fadeOutSchedule?.let { this.handler.removeCallbacks(it) }
	}

	private fun scheduleNextVolumeChange()
	{
		val delay = FADE_OUT_DURATION / INT_VOLUME_MAX

		this.fadeOutSchedule = object : KillableRunnable()
		{
			override fun call()
			{
				scheduleNexFadeOutIteration()
			}
		}
		this.fadeOutSchedule?.let { this.handler.postDelayed(it, delay.toLong()) }
	}

	private fun scheduleNexFadeOutIteration()
	{
		updateVolume(-1)
		if (volume == INT_VOLUME_MIN)
		{
			updateVolume(INT_VOLUME_MAX)
			this.mediaPlayerController.pauseSound()
		}
		else
			scheduleNextVolumeChange()
	}

	private fun updateVolume(change: Int)
	{
		this.volume = this.volume + change

		//ensure volume within boundaries
		if (this.volume < INT_VOLUME_MIN)
			this.volume = INT_VOLUME_MIN
		else if (this.volume > INT_VOLUME_MAX)
			this.volume = INT_VOLUME_MAX

		//convert to float value
		var fVolume = 1 - (Math.log((INT_VOLUME_MAX - this.volume).toDouble()).toFloat() / Math.log(INT_VOLUME_MAX.toDouble()).toFloat())

		//ensure fVolume within boundaries
		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX

		this.mediaPlayerController.volume = fVolume
	}
}