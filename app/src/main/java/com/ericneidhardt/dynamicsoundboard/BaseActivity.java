package com.ericneidhardt.dynamicsoundboard;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ericneidhardt.dynamicsoundboard.mediaplayer.OnMediaPlayersRetrievedCallback;


public class BaseActivity extends Activity
{
	private boolean isActivityVisible = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);
		this.createActionbar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.getMenuInflater().inflate(R.menu.base, menu);
		return true;
	}

	private void createActionbar()
	{
		ActionBar actionBar = this.getActionBar();

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);

		actionBar.setCustomView(R.layout.actionbar);

		actionBar.findViewById(R.id.ib_add_sound).setOnClickListener(this);
		actionBar.findViewById(R.id.ib_add_sound_dir).setOnClickListener(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.isActivityVisible = true;

		this.openSoundFragment("test");
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.isActivityVisible = false;
	}
	
	@Override
	public void onClick(View view)
	{
	}

	private void openSoundFragment(String fragmentId)
	{
		if (!this.isActivityVisible)
			return;

		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		SoundFragment fragment = (SoundFragment) fragmentManager.findFragmentByTag(fragmentId);
		if (fragment != null)
			transaction.replace(R.id.main_frame, fragment, fragmentId);
		else
			transaction.replace(R.id.main_frame, SoundFragment.getNewInstance(fragmentId), fragmentId);

		transaction.commit();
		fragmentManager.executePendingTransactions();
	}
}
