package org.neidhardt.dynamicsoundboard.base

import android.support.v4.app.Fragment
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

abstract class BaseFragment : Fragment()
{
	val baseActivity: SoundActivity
		get() = this.activity as SoundActivity
}

