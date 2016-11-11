package org.neidhardt.dynamicsoundboard.base

import android.support.v4.app.DialogFragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
abstract class BaseDialog : DialogFragment() {
	companion object {
		val KEY_CALLING_FRAGMENT_TAG: String = "KEY_CALLING_FRAGMENT_TAG"
	}

	val soundActivity: SoundActivity
		get() = this.activity as SoundActivity
}

