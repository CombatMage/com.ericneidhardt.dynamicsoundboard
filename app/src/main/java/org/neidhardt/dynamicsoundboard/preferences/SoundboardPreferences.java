package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.Util;

/**
 * File created by eric.neidhardt on 23.01.2015.
 */
public class SoundboardPreferences
{
	public static void registerSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
	}

	public static boolean areNotificationsEnabled()
	{
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_enable_notifications_key), true);
	}

	public static boolean isImmerseModeAvailable()
	{
		if (!Util.IS_KITKAT_AVAILABLE)
			return false;

		Context context = DynamicSoundboardApplication.getSoundboardContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_enable_immerse_mode_key), false);
	}

	public static boolean isOneSwipeToDeleteEnabled()
	{
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.preferences_enable_one_swipe_delete_key), false);
	}

	public static void storePathToSharedPreferences(String path)
	{
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(context.getString(R.string.preferences_enable_one_swipe_delete_key), path);
		editor.apply();
	}

	public static String getPathFromSharedPreferences(String path)
	{
		Context context = DynamicSoundboardApplication.getSoundboardContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(context.getString(R.string.preferences_enable_one_swipe_delete_key), null);
	}
}
