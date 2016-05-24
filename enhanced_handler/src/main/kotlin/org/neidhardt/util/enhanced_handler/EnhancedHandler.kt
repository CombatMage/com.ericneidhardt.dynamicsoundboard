package org.neidhardt.util.enhanced_handler

import android.os.Handler
import java.util.*

/**
 * File created by eric.neidhardt on 23.11.2015.
 */
class EnhancedHandler : Handler()
{
	private val submittedCallbacks: MutableSet<KillableRunnable> = HashSet()
	val pendingCallbacks: Set<KillableRunnable>
		get() = this.submittedCallbacks

	fun removeCallbacks(r: KillableRunnable?)
    {
		if (r == null)
		{
			this.submittedCallbacks.map { it.isKilled = true }
            this.submittedCallbacks.clear()
			super.removeCallbacks(null)
		}
		else
		{
			r.isKilled = true
            this.submittedCallbacks.remove(r)
			super.removeCallbacks(r)
		}
	}

	fun post(r: KillableRunnable): Boolean
	{
		r.handler = this

		val wasSubmitted = super.post(r)
		if (wasSubmitted)
			this.submittedCallbacks.add(r)
		return wasSubmitted
	}

	fun postDelayed(r: KillableRunnable, delayMillis: Long): Boolean
	{
		r.handler = this

		val wasSubmitted = super.postDelayed(r, delayMillis)
		if (wasSubmitted)
			this.submittedCallbacks.add(r)
		return wasSubmitted
	}
}