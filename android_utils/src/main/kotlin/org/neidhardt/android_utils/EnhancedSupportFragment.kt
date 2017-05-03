package org.neidhardt.android_utils

import android.os.Bundle
import com.trello.navi2.Event
import com.trello.navi2.component.support.NaviFragment
import com.trello.navi2.rx.RxNavi
import com.trello.rxlifecycle2.navi.NaviLifecycle
import io.reactivex.Observable


/**
 * Created by eric.neidhardt@gmail.com on 26.04.2017.
 */
abstract class EnhancedSupportFragment : NaviFragment() {

	open var fragmentTag: String = javaClass.name.toString()

	val fragmentLifeCycle = NaviLifecycle.createFragmentLifecycleProvider(this)

	internal var onFirstTimeLaunchedListener: (() -> Unit)? = null
	internal var onRestoreStateListener: ((Bundle) -> Unit)? = null
	internal var onSaveStateListener: ((Bundle) -> Unit)? = null

	private var savedState: Bundle? = null

	init {
		if (this.arguments == null)
			this.arguments = Bundle()

		RxNavi.observe(this, Event.ACTIVITY_CREATED).subscribe {
			if (!this.restoreStateFromArguments()) {
				this.onFirstTimeLaunchedListener?.invoke()
				this.onFirstTimeLaunched()
			}
		}

		RxNavi.observe(this, Event.SAVE_INSTANCE_STATE).subscribe {
			this.saveStateToArguments()
		}

		RxNavi.observe(this, Event.DESTROY_VIEW).subscribe {
			this.saveStateToArguments()
		}
	}

	fun saveStateToArguments() {
		if (this.view != null)
			this.savedState = this.saveState()
		if (this.savedState != null) {
			val b = this.arguments
			b.putBundle("internalSavedViewState_${this.fragmentTag}", savedState)
		}
	}

	private fun restoreStateFromArguments(): Boolean {
		val b = this.arguments
		this.savedState = b.getBundle("internalSavedViewState_${this.fragmentTag}")
		this.savedState?.let { state ->
			this.onRestoreStateListener?.invoke(state)
			this.onRestoreState(state)
			return true
		}
		return false
	}

	private fun saveState(): Bundle {
		val state = Bundle()
		this.onSaveStateListener?.invoke(state)
		this.onSaveState(state)
		return state
	}

	protected open fun onRestoreState(savedInstanceState: Bundle) { }

	protected open fun onSaveState(outState: Bundle) { }

	protected open fun onFirstTimeLaunched() {}
}

object RxEnhancedSupportFragment {

	fun launchesFirstTime(fragment: EnhancedSupportFragment): Observable<EnhancedSupportFragment> {
		return Observable.create { subscriber ->
			val listener = {
				subscriber.onNext(fragment)
			}
			fragment.onFirstTimeLaunchedListener = listener
		}
	}

	fun restoresState(fragment: EnhancedSupportFragment): Observable<Bundle> {
		return Observable.create { subscriber ->
			val listener = { bundle: Bundle ->
				subscriber.onNext(bundle)
			}
			fragment.onRestoreStateListener = listener
		}
	}

	fun savesState(fragment: EnhancedSupportFragment): Observable<Bundle> {
		return Observable.create { subscriber ->
			val listener = { bundle: Bundle ->
				subscriber.onNext(bundle)
			}
			fragment.onSaveStateListener = listener
		}
	}
}
