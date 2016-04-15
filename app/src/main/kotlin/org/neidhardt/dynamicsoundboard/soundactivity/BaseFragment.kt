package org.neidhardt.dynamicsoundboard.soundactivity

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View

abstract class BaseFragment : Fragment()
{
	val baseActivity: SoundActivity
		get() = this.activity as SoundActivity
}

