package org.neidhardt.dynamicsoundboard.base

import com.trello.rxlifecycle2.components.support.RxDialogFragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
abstract class BaseDialogFragment : RxDialogFragment()

abstract class BaseDialog : BaseDialogFragment() {
	companion object {
		val KEY_CALLING_FRAGMENT_TAG: String = "KEY_CALLING_FRAGMENT_TAG"
	}

	val soundActivity: SoundActivity
		get() = this.activity as SoundActivity
}

