package org.neidhardt.dynamicsoundboard.soundactivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.addnewsoundfromintent.AddNewSoundFromIntent;
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.LoadLayoutDialog;
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.StoreLayoutDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.IntentRequest;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity;
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityResumedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;
import org.neidhardt.dynamicsoundboard.views.edittext.ActionbarEditText;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButton;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SoundActivity
		extends
			AppCompatActivity
{
	private static final String TAG = SoundActivity.class.getName();

	private boolean isActivityVisible = true;
	private boolean isActionModeActive = false;

	private DrawerLayout navigationDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;

	private PauseSoundOnCallListener phoneStateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);

		this.initActionbar();
		this.initNavigationDrawer();

		this.addSoundSheetManagerFragment();
		this.addSoundManagerFragment();

		this.phoneStateListener = new PauseSoundOnCallListener();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void setFloatActionButton()
	{
		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.findViewById(R.id.fab_add);
		if (fab == null)
			return;

		ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
		if (serviceManagerFragment != null && serviceManagerFragment.getSoundService() != null)
		{
			Set<EnhancedMediaPlayer> currentlyPlayingSounds = serviceManagerFragment.getSoundService().getCurrentlyPlayingSounds();
			if (currentlyPlayingSounds.size() > 0)
			{
				EventBus.getDefault().postSticky(new ActivitySoundsStateChangedEvent(true));
				return;
			}
		}
		EventBus.getDefault().postSticky(new ActivitySoundsStateChangedEvent(false));
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		this.handleIntent(intent);
	}

	public void handleIntent(Intent intent)
	{
		if (intent == null)
			return;

		String action = intent.getAction();
		if (action == null)
			return;

		if (intent.getAction().equals(Intent.ACTION_VIEW)
				&& intent.getData() != null)
		{
			SoundSheetsManagerFragment fragment = this.getSoundSheetsManagerFragment();
			if (fragment.getSoundSheets().size() == 0)
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(), fragment.getSuggestedSoundSheetName(), null);
			else
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(), fragment.getSuggestedSoundSheetName(), fragment.getSoundSheets());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.getMenuInflater().inflate(R.menu.overflow_menu, menu);
		return true;
	}

	private void initActionbar()
	{
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);

		this.findViewById(R.id.et_set_label).setVisibility(View.GONE);
		this.findViewById(R.id.tv_app_name).setVisibility(View.VISIBLE);
	}

	private void initNavigationDrawer()
	{  // The navigation drawer is fixed on tablets in landscape mode, therefore we need to check the Views type
		View navigationDrawerLayout = this.findViewById(R.id.root_layout);
		if (navigationDrawerLayout != null && navigationDrawerLayout instanceof DrawerLayout)
		{
			this.navigationDrawerLayout = (DrawerLayout)navigationDrawerLayout;
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
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			if (SoundboardPreferences.isImmerseModeAvailable())
				hideSystemUi();
			else
				showSystemUi();
		}
		this.getNavigationDrawerFragment().adjustViewPagerToContent();
	}

	private void showSystemUi()
	{
		this.getWindow().getDecorView().setSystemUiVisibility(Util.SYSTEM_UI_NON_IMMERSE);
		this.findViewById(R.id.v_status_bar_padding).setVisibility(View.VISIBLE);
		this.getNavigationDrawerFragment().calculateMinHeightOfListContent();
	}

	private void hideSystemUi()
	{
		this.getWindow().getDecorView().setSystemUiVisibility(Util.SYSTEM_UI_FULL_IMMERSE);
		this.findViewById(R.id.v_status_bar_padding).setVisibility(View.GONE);
		this.getNavigationDrawerFragment().calculateMinHeightOfListContent();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		this.startService(new Intent(this.getApplicationContext(), MusicService.class));
		EventBus.getDefault().registerSticky(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.isActivityVisible = true;
		this.setSoundSheetActionsEnable(false);
		this.setFloatActionButton();

		SoundSheetFragment currentFragment = getCurrentSoundFragment(this.getFragmentManager());
		if (currentFragment != null)
			currentFragment.notifyDataSetChanged(); // trigger update after return from settings activity

		EventBus.getDefault().postSticky(new ActivityResumedEvent());

		PauseSoundOnCallListener.registerListener(this, this.phoneStateListener, this.getServiceManagerFragment());
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.isActivityVisible = false;

		PauseSoundOnCallListener.unregisterListener(this, this.phoneStateListener);
	}

	@Override
	protected void onStop()
	{
		EventBus.getDefault().unregister(this);
		super.onStop();
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

	/**
	 * This is called by greenRobot EventBus in case a sound sheet was removed.
	 * playlist entries.
	 * @param event delivered SoundSheetsRemovedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundSheetsRemovedEvent event)
	{
		SoundSheet soundSheet = event.getRemovedSoundSheet();
		if (soundSheet == null)
			throw new NullPointerException(TAG + ": onEvent() delivered Data is null " + event);

		this.removeSoundFragment(soundSheet);
		if (soundSheet.getIsSelected())
		{
			List<SoundSheet> remainingSoundSheets = this.getSoundSheetsManagerFragment().getSoundSheets();
			if (remainingSoundSheets.size() > 0)
				this.openSoundFragment(remainingSoundSheets.get(0));
		}
	}

	/**
	 * This is called by greenRobot EventBus in case a mediaplayer changed his state
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(MediaPlayerStateChangedEvent event)
	{
		this.setFloatActionButton();
	}

	/**
	 * This is called by greenRobot EventBus in case a the floating action button was clicked
	 * @param event delivered FabClickedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(FabClickedEvent event)
	{
		Logger.d(TAG, "onEvent: " + event);

		SoundSheetFragment soundSheetFragment = getCurrentSoundFragment(this.getFragmentManager());
		ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
		Set<EnhancedMediaPlayer> currentlyPlayingSounds = serviceManagerFragment.getSoundService().getCurrentlyPlayingSounds();
		if (currentlyPlayingSounds.size() > 0)
		{
			List<EnhancedMediaPlayer> copyCurrentlyPlayingSounds = new ArrayList<>(currentlyPlayingSounds.size());
			copyCurrentlyPlayingSounds.addAll(currentlyPlayingSounds);
			for (EnhancedMediaPlayer sound : copyCurrentlyPlayingSounds)
				sound.pauseSound();
		}
		else if (soundSheetFragment == null)
		{
			((SoundSheetsManagerFragment) this.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG)).openDialogAddNewSoundSheet();
		}
		else
		{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType(FileUtils.MIME_AUDIO);
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
			case R.id.action_preferences:
				this.startActivity(new Intent(this, PreferenceActivity.class));
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing);
				return true;
			case R.id.action_about:
				this.startActivity(new Intent(this, AboutActivity.class));
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing);
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onSupportActionModeStarted(ActionMode mode)
	{
		super.onSupportActionModeStarted(mode);
		this.isActionModeActive = true;
	}

	@Override
	public void onSupportActionModeFinished(ActionMode mode)
	{
		super.onSupportActionModeFinished(mode);
		this.isActionModeActive = false;
	}

	public boolean isActionModeActive()
	{
		return isActionModeActive;
	}

	void closeNavigationDrawer()
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

		Fragment fragment =  fragmentManager.findFragmentByTag(SoundSheetsManagerFragment.TAG);
		if (fragment == null)
		{
			fragment = new SoundSheetsManagerFragment();
			fragmentManager.beginTransaction().add(fragment, SoundSheetsManagerFragment.TAG).commit();
			fragmentManager.executePendingTransactions();
		}
	}

	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	public SoundSheetsManagerFragment getSoundSheetsManagerFragment()
	{
		return (SoundSheetsManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
	}

	public NavigationDrawerFragment getNavigationDrawerFragment()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
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

		if (soundSheet == null)
			throw new NullPointerException("cannot open soundSheetFragment, soundSheet is null");

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

	public static SoundSheetFragment getCurrentSoundFragment(FragmentManager manager)
	{
		Fragment currentFragment = manager.findFragmentById(R.id.main_frame);
		if (currentFragment != null && currentFragment.isVisible() && currentFragment instanceof SoundSheetFragment)
			return (SoundSheetFragment) currentFragment;
		return null;
	}

	public void switchToActiveSoundLayout()
	{
		SoundSheetsManagerFragment fragment = (SoundSheetsManagerFragment) this.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
		this.removeSoundFragment(fragment.getSoundSheets());
		this.setSoundSheetActionsEnable(false);
		fragment.storeSoundSheets();
		fragment.initSoundSheets();

		MusicService service = this.getServiceManagerFragment().getSoundService();
		service.clearAndStoreSoundsAndPlayList();
		service.initSoundsAndPlayList();
	}
}