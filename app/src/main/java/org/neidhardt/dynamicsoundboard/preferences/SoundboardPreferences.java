package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 23.01.2015.
 */
public class SoundboardPreferences
{
	public static void registerSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
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
}
