package org.neidhardt.utils

import android.app.FragmentManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*

/**
 * @author eric.neidhardt on 15.06.2016.
 */
open class EnhancedAppCompatActivity : AppCompatActivity() {

	private val TAG = javaClass.name

	private var isResumed = false

	private val queueOnResume = ArrayList<(activity: EnhancedAppCompatActivity) -> Unit>()

	val isActivityResumed: Boolean get() = this.isResumed

	fun postAfterOnResume(action: (activity: EnhancedAppCompatActivity) -> Unit) {
		if (this.isResumed)
			action.invoke(this)
		else
			this.queueOnResume.push(action)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.isResumed = false
		this.queueOnResume.clear()
	}

	override fun onPostResume() {
		super.onPostResume()
		this.isResumed = true
		while (this.queueOnResume.isNotEmpty()) this.queueOnResume.pop().invoke(this)
	}

	override fun onPause() {
		this.isResumed = false
		super.onPause()
	}

	override fun getFragmentManager(): FragmentManager?
	{
		throw IllegalAccessException("$TAG: Do not use default getFragmentManager, user getSupportFragmentManager instead")
	}
}

private fun <T>ArrayList<T>.push(item: T) {
	this.add(0, item)
}

private fun <T>ArrayList<T>.pop(): T {
	if (this.isEmpty()) throw UnsupportedOperationException("Cannot pop item from queue, because queue is empty")
	return this.removeAt(this.size - 1)
}
