package org.neidhardt.dynamicsoundboard.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 21.01.2015.
 */
public class PreferenceActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_preferences);
		this.createActionbar();

		this.getFragmentManager().beginTransaction().replace(R.id.main_frame, new SoundboardPreferenceFragment()).commit();
	}

	protected void createActionbar()
	{
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == android.R.id.home)
			this.finish();

		return super.onOptionsItemSelected(item);
	}

	public static class SoundboardPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			this.addPreferencesFromResource(R.xml.preferences);
		}
	}
}
