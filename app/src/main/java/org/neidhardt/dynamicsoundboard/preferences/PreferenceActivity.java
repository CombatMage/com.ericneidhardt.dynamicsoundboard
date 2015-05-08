package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 21.01.2015.
 */
public class PreferenceActivity extends AppCompatActivity
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
		ActionBar actionBar = this.getSupportActionBar();
		if (actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == android.R.id.home)
			this.navigateBack();

		return super.onOptionsItemSelected(item);
	}

	private void navigateBack()
	{
		Intent intent = NavUtils.getParentActivityIntent(this);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		NavUtils.navigateUpTo(this, intent);
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out);
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

	@Override
	public void finish()
	{
		super.finish();
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out);
	}
}
