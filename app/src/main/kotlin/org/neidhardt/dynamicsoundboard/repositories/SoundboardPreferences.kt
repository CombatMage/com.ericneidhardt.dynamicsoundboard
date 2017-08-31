package org.neidhardt.dynamicsoundboard.repositories

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication

/**
 * File created by eric.neidhardt on 23.01.2015.
 */
object SoundboardPreferences
{
	fun registerSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
	{
		val context = SoundboardApplication.context
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener)
	}

	fun unregisterSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
	{
		val context = SoundboardApplication.context
		PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener)
	}
}
