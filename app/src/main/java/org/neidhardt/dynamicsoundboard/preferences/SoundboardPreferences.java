package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;

/**
 * File created by eric.neidhardt on 23.01.2015.
 */
public class SoundboardPreferences
{
	public static void registerSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		Context context = DynamicSoundboardApplication.Companion.getContext();
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
		Context context = DynamicSoundboardApplication.Companion.getContext();
		PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
	}

	public static boolean areNotificationsEnabled()
	{
		Context context = DynamicSoundboardApplication.Companion.getContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_enable_notifications_key), true);
	}

	public static boolean isOneSwipeToDeleteEnabled()
	{
		Context context = DynamicSoundboardApplication.Companion.getContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_enable_one_swipe_delete_key), false);
	}

	public static boolean useSystemBrowserForFiles()
	{
		Context context = DynamicSoundboardApplication.Companion.getContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_use_system_file_browser_key), false);
	}

}
