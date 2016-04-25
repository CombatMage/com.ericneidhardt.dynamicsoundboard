package org.neidhardt.dynamicsoundboard.preferences

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

	fun areNotificationsEnabled(): Boolean
	{
		val context = SoundboardApplication.context
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getBoolean(context.getString(R.string.preferences_enable_notifications_key), true)
	}

	val isOneSwipeToDeleteEnabled: Boolean
		get()
		{
			val context = SoundboardApplication.context
			val preferences = PreferenceManager.getDefaultSharedPreferences(context)
			return preferences.getBoolean(context.getString(R.string.preferences_enable_one_swipe_delete_key), false)
		}

	fun useSystemBrowserForFiles(): Boolean
	{
		val context = SoundboardApplication.context
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getBoolean(context.getString(R.string.preferences_use_system_file_browser_key), false)
	}

}
