package org.neidhardt.dynamicsoundboard.soundactivity

import android.app.FragmentManager
import android.support.v7.app.AppCompatActivity

/**
* Created by Eric.Neidhardt@GMail.com on 09.04.2016.
*/
abstract class BaseActivity : AppCompatActivity()
{
	private val TAG = javaClass.name

	var isActivityVisible = true

	override fun onResume() {
		super.onResume()
		this.isActivityVisible = true
	}

	override fun onPause() {
		this.isActivityVisible = false
		super.onPause()
	}

	override fun getFragmentManager(): FragmentManager?
	{
		throw IllegalAccessException("$TAG: Do not use default getFragmentManager, user getSupportFragmentManager instead")
	}
}