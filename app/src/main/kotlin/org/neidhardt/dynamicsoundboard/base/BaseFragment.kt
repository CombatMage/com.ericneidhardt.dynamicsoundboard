package org.neidhardt.dynamicsoundboard.base

import android.os.Bundle
import android.support.v4.app.Fragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity


abstract class BaseFragment : Fragment() {

	val baseActivity: SoundActivity get() = this.activity as SoundActivity

	init {
		if (this.arguments == null)
			this.arguments = Bundle()
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		if (!this.restoreStateFromArguments()) {
			this.onFirstTimeLaunched()
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		saveStateToArguments()
	}

	private var savedState: Bundle? = null

	fun saveStateToArguments() {
		if (this.view != null)
			this.savedState = this.saveState()
		if (this.savedState != null) {
			val b = this.arguments
			b.putBundle("internalSavedViewState8954201239547", savedState)
		}
	}


	private fun restoreStateFromArguments(): Boolean {
		val b = this.arguments
		this.savedState = b.getBundle("internalSavedViewState8954201239547")
		this.savedState?.let { state ->
			this.onRestoreState(state)
			return true
		}
		return false
	}

	private fun saveState(): Bundle {
		val state = Bundle()
		this.onSaveState(state)
		return state
	}

	override fun onDestroyView() {
		super.onDestroyView()
		this.saveStateToArguments()
	}

	protected open fun onRestoreState(savedInstanceState: Bundle) { }

	protected open fun onSaveState(outState: Bundle) { }

	protected open fun onFirstTimeLaunched() {}
}

