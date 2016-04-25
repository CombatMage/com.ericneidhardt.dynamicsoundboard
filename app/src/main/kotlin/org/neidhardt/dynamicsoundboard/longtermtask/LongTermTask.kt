package org.neidhardt.dynamicsoundboard.longtermtask

import android.os.Handler
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.Logger
import roboguice.util.SafeAsyncTask

/**
 * File created by eric.neidhardt on 24.03.2015.
 */

abstract class LongTermTask<T> : SafeAsyncTask<T>()
{
	protected abstract val TAG: String

	private val eventBus = EventBus.getDefault()

	companion object
	{
		private var taskCounter: Int = 0
	}

	@Throws(Exception::class)
	override fun onPreExecute()
	{
		super.onPreExecute()
		taskCounter++
		this.eventBus.postSticky(LongTermTaskStateChangedEvent(true, taskCounter))
	}

	@Throws(Exception::class)
	override fun onSuccess(result: T)
	{
		super.onSuccess(result)
		taskCounter--
		this.eventBus.postSticky(LongTermTaskStateChangedEvent(false, taskCounter))
	}

	@Throws(RuntimeException::class)
	override fun onException(e: Exception)
	{
		super.onException(e)
		Logger.e(TAG, e.message)
		taskCounter--
		this.eventBus.postSticky(LongTermTaskStateChangedEvent(false, taskCounter))
		throw RuntimeException(e)
	}
}

abstract class LoadListTask<T> : LongTermTask<List<T>>()
{
	private val updateHandler = Handler()

	fun postUpdatToMainThread(runnable: () -> Unit)
	{
		this.updateHandler.post(runnable)
	}
}