package org.neidhardt.dynamicsoundboard.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 21.01.2015.
 */
public class AboutActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_preferences);

		this.createActionbar();

		this.getFragmentManager().beginTransaction().replace(R.id.main_frame, new SoundboardAboutFragment()).commit();
	}

	@Override
	protected void createActionbar()
	{
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.about);
		this.setSupportActionBar(toolbar);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public static class SoundboardAboutFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			this.addPreferencesFromResource(R.xml.about);
		}
	}
}
