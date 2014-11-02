package com.ericneidhardt.dynamicsoundboard;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundSheetManagerFragment;

import java.util.List;


public class BaseActivity extends ActionBarActivity implements View.OnClickListener
{
	private static final String TAG = BaseActivity.class.getSimpleName();

	private boolean isActivityVisible = true;

	private DrawerLayout navigationDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;

	private PauseSoundOnCallListener phoneStateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);
		this.createActionbar();
		this.createNavigationDrawer();

		this.addSoundManagerFragment();
		this.addSoundSheetManagerFragment();

		this.addSoundLayoutControllerFragment();

		this.phoneStateListener = new PauseSoundOnCallListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.getMenuInflater().inflate(R.menu.overflow_menu, menu);
		return true;
	}

	private void createActionbar()
	{
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);

		this.findViewById(R.id.action_add_sound).setOnClickListener(this);
		this.findViewById(R.id.action_add_sound_dir).setOnClickListener(this);

		this.findViewById(R.id.et_set_label).setVisibility(View.GONE);
		this.findViewById(R.id.tv_app_name).setVisibility(View.VISIBLE);
	}

	private void createNavigationDrawer()
	{
		this.navigationDrawerLayout = (DrawerLayout)this.findViewById(R.id.root_layout);

		this.drawerToggle = new ActionBarDrawerToggle(this,
				this.navigationDrawerLayout,
				(Toolbar) this.findViewById(R.id.toolbar),
				R.string.navigation_drawer_content_description_open,
				R.string.navigation_drawer_content_description_close);

		this.drawerToggle.setDrawerIndicatorEnabled(true);
		this.navigationDrawerLayout.setDrawerListener(drawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		this.drawerToggle.syncState();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.isActivityVisible = true;
		this.setSoundSheetActionsEnable(false);

		PauseSoundOnCallListener.registerListener(this,
				this.phoneStateListener, (SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG));
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.isActivityVisible = false;

		PauseSoundOnCallListener.unregisterListener(this, this.phoneStateListener);
	}

	public void setSoundSheetActionsEnable(boolean enable)
	{
		int viewState = enable ? View.VISIBLE : View.GONE;
		this.findViewById(R.id.action_add_sound).setVisibility(viewState);
		this.findViewById(R.id.action_add_sound_dir).setVisibility(viewState);
		this.findViewById(R.id.et_set_label).setVisibility(viewState);

		viewState = !enable ? View.VISIBLE : View.GONE;
		this.findViewById(R.id.tv_app_name).setVisibility(viewState);

		if (!enable && this.findViewById(R.id.fab_add) != null)
			this.findViewById(R.id.fab_add).setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.fab_add:
				((SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG)).openDialogAddNewSoundSheet();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		if (this.drawerToggle.onOptionsItemSelected(item))
			return true;

		switch (item.getItemId())
		{
			case R.id.action_settings:
				Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return false;
		}
	}

	public void closeNavigationDrawer()
	{
		if (this.navigationDrawerLayout.isDrawerOpen(Gravity.START))
			this.navigationDrawerLayout.closeDrawer(Gravity.START);
	}

	private void addSoundManagerFragment()
	{
		FragmentManager fragmentManager = this.getFragmentManager();

		Fragment fragment =  fragmentManager.findFragmentByTag(SoundManagerFragment.TAG);
		if (fragment == null)
		{
			fragment = new SoundManagerFragment();
			fragmentManager.beginTransaction().add(fragment, SoundManagerFragment.TAG).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	private void addSoundSheetManagerFragment()
	{
		FragmentManager fragmentManager = this.getFragmentManager();

		Fragment fragment =  fragmentManager.findFragmentByTag(SoundSheetManagerFragment.TAG);
		if (fragment == null)
		{
			fragment = new SoundSheetManagerFragment();
			fragmentManager.beginTransaction().add(fragment, SoundSheetManagerFragment.TAG).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	private void addSoundLayoutControllerFragment()
	{
		FragmentManager fragmentManager = this.getFragmentManager();

		Fragment fragment =  fragmentManager.findFragmentByTag(NavigationDrawerFragment.TAG);
		if (fragment == null)
		{
			fragment = new NavigationDrawerFragment();
			fragmentManager.beginTransaction().add(fragment, NavigationDrawerFragment.TAG).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	public void removeSoundFragment(List<SoundSheet> soundSheets)
	{
		if (soundSheets == null || soundSheets.size() == 0)
			return;

		FragmentManager fragmentManager = this.getFragmentManager();
		for (SoundSheet soundSheet : soundSheets)
		{
			Fragment fragment = fragmentManager.findFragmentByTag(soundSheet.getFragmentTag());
			if (fragment != null)
				fragmentManager.beginTransaction().remove(fragment).commit();
		}
		fragmentManager.executePendingTransactions();
	}

	public void removeSoundFragment(SoundSheet soundSheet)
	{
		FragmentManager fragmentManager = this.getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(soundSheet.getFragmentTag());
		if (fragment != null)
		{
			fragmentManager.beginTransaction().remove(fragment).commit();
			if (fragment.isVisible())
				this.setSoundSheetActionsEnable(false);
		}
		fragmentManager.executePendingTransactions();
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
