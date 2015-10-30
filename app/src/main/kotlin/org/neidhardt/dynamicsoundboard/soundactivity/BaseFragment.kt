package org.neidhardt.dynamicsoundboard.soundactivity


import android.app.Fragment

abstract class BaseFragment : Fragment()
{
	val baseActivity: SoundActivity
		get() = this.activity as SoundActivity
}
