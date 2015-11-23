package org.neidhardt.util.enhanced_handler

import android.os.Handler
import java.util.*

/**
 * File created by eric.neidhardt on 23.11.2015.
 */
class EnhancedHandler : Handler() {

	private val submittedCallbacks: MutableSet<KillableRunnable> = HashSet()

	fun removeCallbacks(r: KillableRunnable?) {
		if (r == null)
		{
			this.submittedCallbacks.map { it.isKilled = true }
			super.removeCallbacks(null)
		}
		else
		{
			r.isKilled = true
			super.removeCallbacks(r)
		}
	}

	fun post(r: KillableRunnable): Boolean
	{
		val wasSubmitted = super.post(r)
		if (wasSubmitted)
			this.submittedCallbacks.add(r)
		return wasSubmitted
	}

	fun postDelayed(r: KillableRunnable, delayMillis: Long): Boolean
	{
		val wasSubmitted = super.postDelayed(r, delayMillis)
		if (wasSubmitted)
			this.submittedCallbacks.add(r)
		return wasSubmitted
	}

}