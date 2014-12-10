package com.ericneidhardt.dynamicsoundboard;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.AddPauseFloatingActionButton;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.LoadLayoutDialog;
import com.ericneidhardt.dynamicsoundboard.dialog.StoreLayoutDialog;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.MusicService;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

import java.util.List;


public class BaseActivity extends ActionBarActivity implements View.OnClickListener
{
	private static final String TAG = BaseActivity.class.getName();

	private boolean isActivityVisible = true;

	private DrawerLayout navigationDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;

	private LocalBroadcastManager broadcastManager;
	private PauseSoundOnCallListener phoneStateListener;
	private SoundStateChangedReceiver soundStateChangedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);

		this.broadcastManager = LocalBroadcastManager.getInstance(this);

		this.createActionbar();
		this.createNavigationDrawer();

		this.addSoundSheetManagerFragment();
		this.addSoundManagerFragment();

		this.addSoundLayoutControllerFragment();

		this.phoneStateListener = new PauseSoundOnCallListener();
		this.soundStateChangedReceiver = new SoundStateChangedReceiver();
	}

	private void setFloatActionButton()
	{
		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.findViewById(R.id.fab_add);
		if (fab == null)
			return;

		fab.setOnClickListener(this);

		ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
		if (serviceManagerFragment != null && serviceManagerFragment.getSoundService() != null)
		{
			List<EnhancedMediaPlayer> currentlyPlayingSounds = serviceManagerFragment.getSoundService().getCurrentlyPlayingSounds();
			if (currentlyPlayingSounds.size() > 0)
			{
				fab.setPauseState();
				return;
			}
		}
		fab.setAddState();
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
		if (this.navigationDrawerLayout != null)
		{
			this.drawerToggle = new ActionBarDrawerToggle(this,
					this.navigationDrawerLayout,
					(Toolbar) this.findViewById(R.id.toolbar),
					R.string.navigation_drawer_content_description_open,
					R.string.navigation_drawer_content_description_close);

			this.drawerToggle.setDrawerIndicatorEnabled(true);
			this.navigationDrawerLayout.setDrawerListener(drawerToggle);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		if (this.drawerToggle != null)
			this.drawerToggle.syncState();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		this.startService(new Intent(this.getApplicationContext(), MusicService.class));
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.isActivityVisible = true;
		this.setSoundSheetActionsEnable(false);

		this.setFloatActionButton();

		PauseSoundOnCallListener.registerListener(this, this.phoneStateListener, this.getServiceManagerFragment());

		this.broadcastManager.registerReceiver(this.soundStateChangedReceiver, EnhancedMediaPlayer.getMediaPlayerIntentFilter());
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.isActivityVisible = false;

		PauseSoundOnCallListener.unregisterListener(this, this.phoneStateListener);
		this.broadcastManager.unregisterReceiver(this.soundStateChangedReceiver);
	}

	@Override
	protected void onUserLeaveHint()
	{
		super.onUserLeaveHint();
		this.getServiceManagerFragment().onUserLeaveHint();
	}

	public void setSoundSheetActionsEnable(boolean enable)
	{
		int viewState = enable ? View.VISIBLE : View.GONE;
		this.findViewById(R.id.action_add_sound).setVisibility(viewState);
		this.findViewById(R.id.action_add_sound_dir).setVisibility(viewState);
		this.findViewById(R.id.et_set_label).setVisibility(viewState);

		viewState = !enable ? View.VISIBLE : View.GONE;
		this.findViewById(R.id.tv_app_name).setVisibility(viewState);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.fab_add:
				this.onFabClicked();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	private void onFabClicked()
	{
		SoundSheetFragment soundSheetFragment = this.getCurrentFragment();
		ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
		List<EnhancedMediaPlayer> currentlyPlayingSounds = serviceManagerFragment.getSoundService().getCurrentlyPlayingSounds();
		if (currentlyPlayingSounds.size() > 0)
		{
			for (EnhancedMediaPlayer sound : currentlyPlayingSounds)
				sound.pauseSound();
			serviceManagerFragment.notifySoundSheetFragments();
			serviceManagerFragment.notifyPlaylist();
		}
		else if (soundSheetFragment == null)
		{
			((SoundSheetManagerFragment) this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG)).openDialogAddNewSoundSheet();
		}
		else
		{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType(Util.MIME_AUDIO);
			soundSheetFragment.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		if (this.drawerToggle != null && this.drawerToggle.onOptionsItemSelected(item))
			return true;

		switch (item.getItemId())
		{
			case R.id.action_load_sound_sheets:
				LoadLayoutDialog.showInstance(this.getFragmentManager());
				return true;
			case R.id.action_store_sound_sheets:
				StoreLayoutDialog.showInstance(this.getFragmentManager());
				return true;
			case R.id.action_settings:
				Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return false;
		}
	}

	public void closeNavigationDrawer()
	{
		if (this.navigationDrawerLayout == null)
			return;
		if (this.navigationDrawerLayout.isDrawerOpen(Gravity.START))
			this.navigationDrawerLayout.closeDrawer(Gravity.START);
	}

	private void addSoundManagerFragment()
	{
		FragmentManager fragmentManager = this.getFragmentManager();

		Fragment fragment =  fragmentManager.findFragmentByTag(ServiceManagerFragment.TAG);
		if (fragment == null)
		{
			fragment = new ServiceManagerFragment();
			fragmentManager.beginTransaction().add(fragment, ServiceManagerFragment.TAG).commit();
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

		this.closeNavigationDrawer();

		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		SoundSheetFragment fragment = (SoundSheetFragment) fragmentManager.findFragmentByTag(soundSheet.getFragmentTag());
		if (fragment != null)
			transaction.replace(R.id.main_frame, fragment, soundSheet.getFragmentTag());
		else
			transaction.replace(R.id.main_frame, SoundSheetFragment.getNewInstance(soundSheet), soundSheet.getFragmentTag());

		transaction.commit();
		fragmentManager.executePendingTransactions();

		((ActionbarEditText) this.findViewById(R.id.et_set_label)).setText(soundSheet.getLabel());
	}

	public SoundSheetFragment getCurrentFragment()
	{
		FragmentManager manager = this.getFragmentManager();
		Fragment currentFragment = manager.findFragmentById(R.id.main_frame);
		if (currentFragment != null && currentFragment.isVisible() && currentFragment instanceof SoundSheetFragment)
			return (SoundSheetFragment) currentFragment;
		return null;
	}

	private ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	private class SoundStateChangedReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			setFloatActionButton();
		}
	}
}
