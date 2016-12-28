package org.neidhardt.dynamicsoundboard.soundcontrol.views

import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.util.enhanced_handler.EnhancedHandler
import org.neidhardt.util.enhanced_handler.KillableRunnable
import java.util.*

/**
 * @author eric.neidhardt on 11.06.2016.
 */
private val DELETION_TIMEOUT = 5000

class PendingDeletionHandler (
		private val soundPresenter: SoundPresenter,
		private val manager: NewSoundManager,
		private val onItemDeletionRequested: (deletionHandler: PendingDeletionHandler, timeTillDeletion: Int) -> Unit
) {
	private val handler = EnhancedHandler()
	private var deletionTask: KillableRunnable? = null
	private val pendingDeletions = ArrayList<MediaPlayerController>()

	val countPendingDeletions: Int get() = this.pendingDeletions.size

	fun requestItemDeletion(item: MediaPlayerController)
	{
		this.deletionTask?.let { this.handler.removeCallbacks(it) }

		item.isDeletionPending = true
		if (item.isPlayingSound) item.stopSound()

		this.pendingDeletions.add(item)
		this.deletionTask = KillableRunnable({ deletePendingItems() })
		this.deletionTask?.let { this.handler.postDelayed(it, DELETION_TIMEOUT.toLong()) }

		this.onItemDeletionRequested(this, DELETION_TIMEOUT)
	}

	private fun deletePendingItems()
	{
		this.deletionTask?.let { this.handler.removeCallbacks(it) }
		this.manager.remove(this.soundPresenter.soundSheet, this.pendingDeletions)
		this.pendingDeletions.clear()
	}

	fun restoreDeletedItems()
	{
		this.deletionTask?.let { this.handler.removeCallbacks(it) }
		this.pendingDeletions.map { item ->
			item.isDeletionPending = false
			soundPresenter.adapter?.notifyItemChanged(item)
		}
		this.pendingDeletions.clear()
	}
}
