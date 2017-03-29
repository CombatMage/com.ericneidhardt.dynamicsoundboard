package org.neidhardt.dynamicsoundboard.base

import android.content.Intent
import org.neidhardt.android_utils.EnhancedAppCompatActivity
import rx.Observable
import rx.lang.kotlin.add
import java.util.*

/**
* Created by Eric.Neidhardt@GMail.com on 09.04.2016.
*/
abstract class BaseActivity : EnhancedAppCompatActivity() {

	internal val onNewIntentCallback: MutableList<(Intent) -> Unit> = ArrayList()

	internal var lastReceivedIntent: Intent? = null

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)

		intent?: return

		this.lastReceivedIntent = intent
		this.onNewIntentCallback.forEach { it.invoke(intent) }
	}
}

object RxBaseActivity {
	fun receivesNewIntent(activity: BaseActivity): Observable<Intent> {
		return Observable.create<Intent> { subscriber ->
			val listener = { intent: Intent ->
				subscriber.onNext(intent)
			}
			subscriber.add {
				activity.onNewIntentCallback.remove(listener)
			}
			activity.lastReceivedIntent?.let { subscriber.onNext(it) }
			activity.onNewIntentCallback.add(listener)
		}
	}
}