package org.neidhardt.dynamicsoundboard.repositories

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 31.08.2017.
 */
class UserPreferenceRepository(context: Context) {

	private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

	private val keyNotifications = context.getString(R.string.preferences_enable_notifications_key)
	private val keySwipeToDelete = context.getString(R.string.preferences_enable_one_swipe_delete_key)
	private val keyUseSystemBrowser = context.getString(R.string.preferences_use_system_file_browser_key)

	val isNotificationEnabled: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.keyNotifications,
				true)
	}

	val isOneSwipeToDeleteEnabled: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.keySwipeToDelete,
				false)
	}

	val useBuildInBrowserForFiles: Boolean get() {
		return this.preferenceManager.getBoolean(
				this.keyUseSystemBrowser,
				true)
	}

	fun registerSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
		this.preferenceManager.registerOnSharedPreferenceChangeListener(listener)
	}

	fun unregisterSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
		this.preferenceManager.unregisterOnSharedPreferenceChangeListener(listener)
	}
}