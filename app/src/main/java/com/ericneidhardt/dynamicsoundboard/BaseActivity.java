package com.ericneidhardt.dynamicsoundboard;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;


public class BaseActivity extends Activity implements View.OnClickListener
{
	private static final String TAG = BaseActivity.class.getSimpleName();

	private boolean isActivityVisible = true;

	private DrawerLayout navigationDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);
		this.createActionbar();
		this.createNavigationDrawer();

		this.addSoundLayoutControllerFragment();
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

		this.findViewById(R.id.action_open_navigation).setOnClickListener(this);
		this.findViewById(R.id.action_add_sound).setOnClickListener(this);
		this.findViewById(R.id.action_add_sound_dir).setOnClickListener(this);
	}

	private void createNavigationDrawer()
	{
		this.navigationDrawerLayout = (DrawerLayout)this.findViewById(R.id.root_layout);
		this.navigationDrawerLayout.setScrimColor(Color.TRANSPARENT);

		DrawerLayout.DrawerListener onNavigationToggleListener = new ActionBarDrawerToggle(this, this.navigationDrawerLayout, R.color.primary_500, R.string.blank, R.string.blank)
		{
			@Override
			public void onDrawerOpened(View drawerView)
			{
				findViewById(R.id.action_open_navigation).setSelected(true);
			}

			@Override
			public void onDrawerClosed(View drawerView)
			{
				findViewById(R.id.action_open_navigation).setSelected(false);
			}
		};
		this.navigationDrawerLayout.setDrawerListener(onNavigationToggleListener);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.isActivityVisible = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.isActivityVisible = false;
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_open_navigation:
				this.toggleNavigationDrawer();
				break;
			case R.id.action_add_sound:
				Toast.makeText(this, "action_add_sound", Toast.LENGTH_SHORT).show();
				break;
			case R.id.action_add_sound_dir:
				Toast.makeText(this, "action_add_sound_dir", Toast.LENGTH_SHORT).show();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_settings:
				Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	private void toggleNavigationDrawer()
	{
		if (this.navigationDrawerLayout.isDrawerOpen(Gravity.START)) {
			this.findViewById(R.id.action_open_navigation).setSelected(false);
			this.navigationDrawerLayout.closeDrawer(Gravity.START);
		}
		else
		{
			this.findViewById(R.id.action_open_navigation).setSelected(true);
			this.navigationDrawerLayout.openDrawer(Gravity.START);
		}
	}

	private void addSoundLayoutControllerFragment()
	{
		FragmentManager fragmentManager = getFragmentManager();

		Fragment fragment =  fragmentManager.findFragmentByTag(SoundSheetManagerFragment.TAG);
		if (fragment == null)
		{
			fragment = new SoundSheetManagerFragment();
			fragmentManager.beginTransaction().add(fragment, SoundSheetManagerFragment.TAG).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	public void openSoundFragment(SoundSheet soundSheet)
	{
		if (!this.isActivityVisible)
			return;

		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		SoundSheetFragment fragment = (SoundSheetFragment) fragmentManager.findFragmentByTag(soundSheet.getFragmentTag());
		if (fragment != null)
			transaction.replace(R.id.main_frame, fragment, soundSheet.getFragmentTag());
		else
			transaction.replace(R.id.main_frame, SoundSheetFragment.getNewInstance(soundSheet), soundSheet.getFragmentTag());

		transaction.commit();
		fragmentManager.executePendingTransactions();
	}
}
