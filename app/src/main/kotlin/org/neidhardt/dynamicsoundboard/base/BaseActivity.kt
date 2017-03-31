package org.neidhardt.dynamicsoundboard.base

import android.content.Intent
import android.os.Bundle
import org.neidhardt.android_utils.EnhancedAppCompatActivity
import io.reactivex.Observable

/**
* Created by Eric.Neidhardt@GMail.com on 09.04.2016.
*/
abstract class BaseActivity : EnhancedAppCompatActivity() {

	internal val onNewIntentCallback: MutableList<(Intent) -> Unit> = ArrayList()

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		val intent = this.intent
		if (intent != null)
			this.onNewIntentCallback.forEach { it.invoke(intent) }
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null)
			this.onNewIntentCallback.forEach { it.invoke(intent) }
	}
}

object RxBaseActivity {
	fun receivesNewIntent(activity: BaseActivity): Observable<Intent> {
		return Observable.create<Intent> { subscriber ->
			val listener = { intent: Intent ->
				subscriber.onNext(intent)
			}

			//subscriber.add(Subscriptions.create {
			//	activity.onNewIntentCallback.remove(listener)
			//})

			activity.onNewIntentCallback.add(listener)
		}
	}
}