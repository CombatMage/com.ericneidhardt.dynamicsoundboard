package org.neidhardt.dynamicsoundboard.base

import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity
import rx.subscriptions.CompositeSubscription

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
abstract class RxDialogFragment : DialogFragment() {

	protected var subscriptions = CompositeSubscription()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.subscriptions = CompositeSubscription()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		this.subscriptions.unsubscribe()
	}
}

abstract class BaseDialog : RxDialogFragment() {
	companion object {
		val KEY_CALLING_FRAGMENT_TAG: String = "KEY_CALLING_FRAGMENT_TAG"
	}

	val soundActivity: SoundActivity
		get() = this.activity as SoundActivity
}

