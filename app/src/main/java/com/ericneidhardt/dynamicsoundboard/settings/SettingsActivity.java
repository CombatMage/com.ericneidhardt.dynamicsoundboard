package com.ericneidhardt.dynamicsoundboard.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 21.01.2015.
 */
public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			this.addPreferencesFromResource(R.xml.preferences);
		}
	}
}
