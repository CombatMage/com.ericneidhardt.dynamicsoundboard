@file:Suppress("unused")

package org.neidhardt.android_utils

import android.os.Bundle
import android.support.v4.app.Fragment


/**
 * Created by eric.neidhardt@gmail.com on 26.04.2017.
 */
abstract class EnhancedSupportFragment : Fragment() {

	open var fragmentTag: String = javaClass.name.toString()

	private var onFirstTimeLaunchedListener: (() -> Unit)? = null
	private var onRestoreStateListener: ((Bundle) -> Unit)? = null
	private var onSaveStateListener: ((Bundle) -> Unit)? = null

	private var savedState: Bundle? = null

	init {
		if (this.arguments == null)
			this.arguments = Bundle()
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		if (!this.restoreStateFromArguments()) {
			this.onFirstTimeLaunchedListener?.invoke()
			this.onFirstTimeLaunched()
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)

		this.saveStateToArguments()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		this.saveStateToArguments()
	}

	private fun saveStateToArguments() {
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
