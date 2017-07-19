package org.neidhardt.android_utils

import android.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import com.trello.navi2.component.support.NaviAppCompatActivity
import io.reactivex.Observable
import java.util.*


/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
open class EnhancedAppCompatActivity : NaviAppCompatActivity() {

	private val TAG = javaClass.name

	val isActivityResumed: Boolean get() = this.isResumed

	private var isResumed = false
	private val queueOnResume = ArrayList<(activity: EnhancedAppCompatActivity) -> Unit>()

	internal var onIntentCallback: ((Intent) -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.isResumed = false
		this.queueOnResume.clear()
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		val intent = this.intent
		if (intent != null)
			this.onIntentCallback?.invoke(intent)
	}

	override fun onResume() {
		super.onResume()
		this.isResumed = true
		while (this.queueOnResume.isNotEmpty()) this.queueOnResume.pop().invoke(this)
	}

	override fun onPause() {
		super.onPause()
		this.isResumed = false
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null)
			this.onIntentCallback?.invoke(intent)
	}

	fun postAfterOnResume(action: (activity: EnhancedAppCompatActivity) -> Unit) {
		if (this.isResumed)
			action.invoke(this)
		else
			this.queueOnResume.push(action)
	}

	override fun getFragmentManager(): FragmentManager? {
		throw IllegalAccessException("$TAG: Do not use default getFragmentManager, use getSupportFragmentManager instead")
	}
}

object RxEnhancedAppCompatActivity {
	fun receivesIntent(activity: EnhancedAppCompatActivity): Observable<Intent> {
		return Observable.create<Intent> { subscriber ->
			val listener = { intent: Intent ->
				subscriber.onNext(intent)
			}
			activity.onIntentCallback = listener
		}
	}
}

private fun <T> ArrayList<T>.push(item: T) {
	this.add(0, item)
}

private fun <T> ArrayList<T>.pop(): T {
	if (this.isEmpty()) throw UnsupportedOperationException("Cannot pop item from queue, because queue is empty")
	return this.removeAt(this.size - 1)
}
