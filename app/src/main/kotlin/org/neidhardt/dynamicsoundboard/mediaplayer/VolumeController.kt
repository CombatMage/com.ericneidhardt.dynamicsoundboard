package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.enhancedhandler.EnhancedHandler
import org.neidhardt.enhancedhandler.KillableRunnable
import org.neidhardt.app_utils.letThis

/**
 * File created by eric.neidhardt on 11.04.2016.
 */
private const val FADE_OUT_DURATION = 100.toLong()
private const val INT_VOLUME_MAX = 100
private const val INT_VOLUME_MIN = 0
private const val FLOAT_VOLUME_MAX = 1f
private const val FLOAT_VOLUME_MIN = 0f

class VolumeController(private val mediaPlayerController: MediaPlayerController) {

	private var volume: Int = INT_VOLUME_MAX
	private var fadeOutSchedule: KillableRunnable? = null

	private val handler = EnhancedHandler()

	val maxVolume = FLOAT_VOLUME_MAX

	var isFadeoutInProgress = false
		private set

	fun fadeOutSound() {
		this.cancelFadeOut()
		this.isFadeoutInProgress = true
		this.updateVolume(0)
		this.scheduleNextVolumeChange()
	}

	fun cancelFadeOut() {
		this.fadeOutSchedule?.let { this.handler.removeCallbacks(it) }
		this.isFadeoutInProgress = false
	}

	private fun scheduleNextVolumeChange() {
		val delay = FADE_OUT_DURATION / INT_VOLUME_MAX

		this.fadeOutSchedule = KillableRunnable {
			scheduleNexFadeOutIteration()
		}.letThis { this.handler.postDelayed(it, delay) }
	}

	private fun scheduleNexFadeOutIteration() {
		this.updateVolume(-1)
		if (volume == INT_VOLUME_MIN)
		{
			this.updateVolume(INT_VOLUME_MAX)
			this.isFadeoutInProgress = false
			this.mediaPlayerController.pauseSound()
		}
		else
			this.scheduleNextVolumeChange()
	}

	private fun updateVolume(change: Int) {
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