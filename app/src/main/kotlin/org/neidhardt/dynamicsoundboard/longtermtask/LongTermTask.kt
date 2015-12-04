package org.neidhardt.dynamicsoundboard.longtermtask

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import roboguice.util.SafeAsyncTask

/**
 * File created by eric.neidhardt on 24.03.2015.
 */

abstract class LongTermTask<T> : SafeAsyncTask<T>()
{
	protected abstract val TAG: String

	companion object
	{
		private var taskCounter: Int = 0
	}

	@Throws(Exception::class)
	override fun onPreExecute()
	{
		super.onPreExecute()
		taskCounter++
		EventBus.getDefault().postSticky(LongTermTaskStateChangedEvent(true, taskCounter))
	}

	@Throws(Exception::class)
	override fun onSuccess(result: T)
	{
		super.onSuccess(result)
		taskCounter--
		EventBus.getDefault().postSticky(LongTermTaskStateChangedEvent(false, taskCounter))
	}

	@Throws(RuntimeException::class)
	override fun onException(e: Exception)
	{
		super.onException(e)
		Logger.e(TAG, e.message)
		taskCounter--
		EventBus.getDefault().postSticky(LongTermTaskStateChangedEvent(false, taskCounter))
		throw RuntimeException(e)
	}
}
