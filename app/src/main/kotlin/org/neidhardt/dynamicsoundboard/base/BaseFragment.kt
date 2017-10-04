package org.neidhardt.dynamicsoundboard.base

import org.neidhardt.android_utils.EnhancedSupportFragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity


abstract class BaseFragment : EnhancedSupportFragment() {
	val baseActivity: SoundActivity get() = this.activity as SoundActivity
}

