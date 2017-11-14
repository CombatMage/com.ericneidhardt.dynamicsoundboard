package org.neidhardt.dynamicsoundboard.repositories

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 31.08.2017.
 */
class PreferenceRepository(private val context: Context) {

	private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

	val isNotificationEnabled: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.context.getString(R.string.preferences_enable_notifications_key),
				true)
	}

	val isOneSwipeToDeleteEnabled: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.context.getString(R.string.preferences_enable_one_swipe_delete_key),
				false)
	}

	val useBuildInBrowserForFiles: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.context.getString(R.string.preferences_use_system_file_browser_key),
				true)
	}

	fun registerSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
		this.preferenceManager.registerOnSharedPreferenceChangeListener(listener)
	}

	fun unregisterSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
		this.preferenceManager.unregisterOnSharedPreferenceChangeListener(listener)
	}
}