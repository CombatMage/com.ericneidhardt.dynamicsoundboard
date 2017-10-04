package org.neidhardt.util.enhanced_handler

/**
 * File created by eric.neidhardt on 23.11.2015.
 */
class KillableRunnable(private val task: () -> Unit) : Runnable {

	var handler: EnhancedHandler? = null

	@Volatile
	var isKilled: Boolean = false

	override fun run() {
		if (this.handler == null)
			throw UnsupportedOperationException(
					"KillableRunnable should be post on EnhancedHandler using either post or postDelayed")

		if (!isKilled)
			this.task()

		this.handler?.removeCallbacks(this)
	}
}