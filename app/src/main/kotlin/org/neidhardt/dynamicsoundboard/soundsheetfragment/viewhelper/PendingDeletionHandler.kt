package org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper

import org.neidhardt.dynamicsoundboard.manager.SoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.enhancedhandler.EnhancedHandler
import org.neidhardt.enhancedhandler.KillableRunnable
import java.util.*

/**
 * @author eric.neidhardt on 11.06.2016.
 */
class PendingDeletionHandler (
		private val soundSheet: SoundSheet,
		private val adapter: SoundAdapter,
		private val manager: SoundManager,
		private val onItemDeletionRequested: (deletionHandler: PendingDeletionHandler, timeTillDeletion: Int) -> Unit
) {
	val DELETION_TIMEOUT = 5000

	private val handler = EnhancedHandler()
	private var deletionTask: KillableRunnable? = null
	private val pendingDeletions = ArrayList<MediaPlayerController>()

	val countPendingDeletions: Int get() = this.pendingDeletions.size

	fun requestItemDeletion(item: MediaPlayerController) {
		this.deletionTask?.let { this.handler.removeCallbacks(it) }

		item.isDeletionPending = true
		if (item.isPlayingSound) item.stopSound()

		this.pendingDeletions.add(item)
		this.deletionTask = KillableRunnable({ deletePendingItems() })
		this.deletionTask?.let { this.handler.postDelayed(it, DELETION_TIMEOUT.toLong()) }

		this.onItemDeletionRequested(this, DELETION_TIMEOUT)
	}

	private fun deletePendingItems() {
		this.deletionTask?.let { this.handler.removeCallbacks(it) }
		this.manager.remove(this.soundSheet, this.pendingDeletions)
		this.pendingDeletions.clear()
	}

	fun restoreDeletedItems() {
		this.deletionTask?.let { this.handler.removeCallbacks(it) }
		this.pendingDeletions.map { item ->
			item.isDeletionPending = false
			this.adapter.notifyItemChanged(item)
		}
		this.pendingDeletions.clear()
	}
}
