package org.neidhardt.dynamicsoundboard.soundactivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.jetbrains.annotations.NotNull;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog;
import org.neidhardt.dynamicsoundboard.fileexplorer.LoadLayoutDialog;
import org.neidhardt.dynamicsoundboard.fileexplorer.StoreLayoutDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.IntentRequest;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OnActionModeChangeRequestedEventListener;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnOpenSoundLayoutSettingsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnSoundLayoutSelectedEventListener;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.notifications.service.NotificationService;
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity;
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity;
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteAllSoundSheetsDialog;
import org.neidhardt.dynamicsoundboard.views.edittext.ActionbarEditText;
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SoundActivity
		extends
			AppCompatActivity
		implements
			View.OnClickListener,
			CustomEditText.OnTextEditedListener,
			OnActionModeChangeRequestedEventListener,
			OnSoundLayoutSelectedEventListener,
			OnOpenSoundLayoutSettingsEvent,
			OnSoundSheetOpenEventListener,
			OnSoundSheetsInitEventLisenter,
			OnSoundSheetsChangedEventListener
{
	private static final String TAG = SoundActivity.class.getName();

	private boolean isActivityVisible = true;
	private boolean isActionModeActive = false;

	private DrawerLayout navigationDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private android.support.v7.view.ActionMode actionMode;

	private PauseSoundOnCallListener phoneStateListener;

	private EventBus eventBus = EventBus.getDefault();

	private SoundsDataAccess soundsDataAccess = DynamicSoundboardApplication.Companion.getSoundsDataAccess();
	private SoundsDataStorage soundsDataStorage = DynamicSoundboardApplication.Companion.getSoundsDataStorage();
	private SoundsDataUtil soundsDataUtil = DynamicSoundboardApplication.Companion.getSoundsDataUtil();

	private SoundSheetsDataAccess soundSheetsDataAccess = DynamicSoundboardApplication.Companion.getSoundSheetsDataAccess();
	private SoundSheetsDataUtil soundSheetsDataUtil = DynamicSoundboardApplication.Companion.getSoundSheetsDataUtil();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_base);

		this.soundsDataUtil.initIfRequired();
		this.soundSheetsDataUtil.initIfRequired();

		this.initActionbar();
		this.initNavigationDrawer();

		this.phoneStateListener = new PauseSoundOnCallListener();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
			if (this.soundSheetsDataAccess.getSoundSheets().size() == 0)
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(),
						this.soundSheetsDataUtil.getSuggestedName(), null);
			else
				AddNewSoundFromIntent.showInstance(this.getFragmentManager(), intent.getData(),
						this.soundSheetsDataUtil.getSuggestedName(), soundSheetsDataAccess.getSoundSheets());
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

		this.findViewById(R.id.tv_app_name).setVisibility(View.VISIBLE);
		this.findViewById(R.id.action_add_sound_sheet).setOnClickListener(this);

		ActionbarEditText soundSheetLabel = (ActionbarEditText) this.findViewById(R.id.et_set_label);
		soundSheetLabel.setVisibility(View.GONE);
		soundSheetLabel.setOnTextEditedListener(this);

		SoundSheetFragment currentSoundSheetFragment = this.getCurrentSoundFragment();
		if (currentSoundSheetFragment != null)
		{
			SoundSheet currentActiveSoundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(currentSoundSheetFragment.getFragmentTag());
			if (currentActiveSoundSheet != null)
				soundSheetLabel.setText(currentActiveSoundSheet.getLabel());
		}
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
					R.string.navigation_drawer_content_description_close) {

				// override onDrawerSlide and pass 0 to super disable arrow animation
				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					super.onDrawerSlide(drawerView, 0);
				}
			};

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
		this.eventBus.registerSticky(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		PauseSoundOnCallListener.registerListener(this, this.phoneStateListener);

		this.startService(new Intent(this.getApplicationContext(), NotificationService.class));

		this.isActivityVisible = true;
		this.eventBus.postSticky(new ActivityStateChangedEvent(true));

		this.setSoundSheetActionsEnable(false);

		this.soundsDataUtil.initIfRequired();
		if (this.soundSheetsDataUtil.initIfRequired())
			this.onSoundSheetsInit();
	}

	private void onSoundSheetsInit()
	{
		this.handleIntent(this.getIntent()); // sound sheets have been loaded, check if there is pending intent to handle
		this.openSoundFragment(this.soundSheetsDataAccess.getSelectedItem());
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		this.isActivityVisible = false;

		PauseSoundOnCallListener.unregisterListener(this, this.phoneStateListener);
	}

	@Override
	protected void onUserLeaveHint()
	{
		super.onUserLeaveHint();
		this.eventBus.postSticky(new ActivityStateChangedEvent(false));
	}

	@Override
	protected void onStop()
	{
		this.eventBus.unregister(this);

		if (this.isFinishing())
		{
			// we remove all loaded sounds, which have no corresponding SoundSheet
			Set<String> fragmentsWithLoadedSounds = this.soundsDataAccess.getSounds().keySet();
			Set<String> fragmentsWithLoadedSoundsToRemove = new HashSet<>();

			for (String fragmentTag : fragmentsWithLoadedSounds)
			{
				if (this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag) == null) // no sound sheet exists
					fragmentsWithLoadedSoundsToRemove.add(fragmentTag);
			}
			for (String fragmentTag : fragmentsWithLoadedSoundsToRemove)
				this.soundsDataStorage.removeSounds(this.soundsDataAccess.getSoundsInFragment(fragmentTag));
		}

		super.onStop();
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
	public void onEvent(@NonNull SoundLayoutSelectedEvent event)
	{
		this.removeSoundFragments(this.soundSheetsDataAccess.getSoundSheets());
		this.setSoundSheetActionsEnable(false);
		this.soundSheetsDataUtil.initIfRequired();

		this.soundsDataUtil.release();
		this.soundsDataUtil.initIfRequired();
	}
	@Override
	public void onEvent(@NonNull OpenSoundLayoutSettingsEvent event)
	{
		SoundLayoutSettingsDialog.Companion.showInstance(this.getFragmentManager(), event.getSoundLayout().getDatabaseId());
	}

	@Override
	public void onEvent(@NonNull OpenSoundSheetEvent event)
	{
		this.openSoundFragment(event.getSoundSheetToOpen());
	}

	@Override
	public void onEvent(@NonNull SoundSheetsInitEvent event)
	{
		this.onSoundSheetsInit();
	}

	@Override
	public void onEventMainThread(@NotNull SoundSheetsRemovedEvent event)
	{
		List<SoundSheet> removedSoundSheets = event.getSoundSheets();
		this.removeSoundFragments(removedSoundSheets);

		if (this.soundSheetsDataAccess.getSoundSheets().size() == 0)
			this.setSoundSheetActionsEnable(false);
	}

	@Override
	public void onEventMainThread(@NotNull SoundSheetAddedEvent event) {}

	@Override
	public void onEventMainThread(@NotNull SoundSheetChangedEvent event) {}

	@Override
	public void onEvent(ActionModeChangeRequestedEvent event)
	{
		ActionModeChangeRequestedEvent.REQUEST requestedAction = event.getRequestedAction();
		if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.START)
		{
			this.actionMode = this.startSupportActionMode(event.getActionModeCallback());
			return;
		}
		if (this.actionMode != null)
		{
			if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.STOP)
			{
				this.actionMode.finish();
				this.actionMode = null;
			}
			else if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.INVALIDATE)
			{
				this.actionMode.invalidate();
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case a the floating action button was clicked
	 * @param event delivered FabClickedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(FabClickedEvent event)
	{
		Logger.d(TAG, "onEvent: " + event);

		SoundSheetFragment soundSheetFragment = this.getCurrentSoundFragment();
		Set<EnhancedMediaPlayer> currentlyPlayingSounds = this.soundsDataAccess.getCurrentlyPlayingSounds();
		if (currentlyPlayingSounds.size() > 0)
		{
			List<EnhancedMediaPlayer> copyCurrentlyPlayingSounds = new ArrayList<>(currentlyPlayingSounds.size());
			copyCurrentlyPlayingSounds.addAll(currentlyPlayingSounds);
			for (EnhancedMediaPlayer sound : copyCurrentlyPlayingSounds)
				sound.pauseSound();
		}
		else if (soundSheetFragment == null)
		{
			AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), this.soundSheetsDataUtil.getSuggestedName());
		}
		else
		{
			if (SoundboardPreferences.useSystemBrowserForFiles())
			{
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(FileUtils.MIME_AUDIO);
				soundSheetFragment.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE);
			}
			else
			{
				SoundSheetFragment currentSoundSheet = this.getCurrentSoundFragment();
				if (currentSoundSheet != null)
					AddNewSoundFromDirectoryDialog.showInstance(this.getFragmentManager(), currentSoundSheet.getFragmentTag());
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case creating a new sound failed.
	 * @param event delivered CreatingPlayerFailedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(CreatingPlayerFailedEvent event)
	{
		String message = getResources().getString(R.string.music_service_loading_sound_failed) + " "
				+ FileUtils.getFileNameFromUri(getApplicationContext(), event.getFailingPlayerData().getUri());
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
				StoreLayoutDialog.Companion.showInstance(this.getFragmentManager());
				return true;
			case R.id.action_preferences:
				this.startActivity(new Intent(this, PreferenceActivity.class));
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing);
				return true;
			case R.id.action_about:
				this.startActivity(new Intent(this, AboutActivity.class));
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing);
				return true;
			case R.id.action_clear_sound_sheets:
				ConfirmDeleteAllSoundSheetsDialog.showInstance(this.getFragmentManager());
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onClick(@NonNull View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound_sheet:
				AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), this.soundSheetsDataUtil.getSuggestedName());
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	@Override
	public void onTextEdited(String text)
	{
		SoundSheetFragment currentSoundSheetFragment = this.getCurrentSoundFragment();
		if (currentSoundSheetFragment != null)
		{
			SoundSheet soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(currentSoundSheetFragment.getFragmentTag());
			if (soundSheet != null)
			{
				soundSheet.setLabel(text);
				soundSheet.updateItemInDatabaseAsync();
				this.eventBus.post(new SoundSheetChangedEvent(soundSheet));
			}
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

	@SuppressWarnings("ResourceType") // for unknown reason, inspection demand using Gravity.START, but this would leed to warnings
	void closeNavigationDrawer()
	{
		if (this.navigationDrawerLayout == null)
			return;
		if (this.navigationDrawerLayout.isDrawerOpen(Gravity.START))
			this.navigationDrawerLayout.closeDrawer(Gravity.START);
	}

	public NavigationDrawerFragment getNavigationDrawerFragment()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
	}

	public void removeSoundFragments(List<SoundSheet> soundSheets)
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

		if (this.soundSheetsDataAccess.getSoundSheets().size() == 0)
			this.setSoundSheetActionsEnable(false);
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
			return;

		this.closeNavigationDrawer();

		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		SoundSheetFragment fragment = (SoundSheetFragment) fragmentManager.findFragmentByTag(soundSheet.getFragmentTag());
		if (fragment != null)
			transaction.replace(R.id.main_frame, fragment, soundSheet.getFragmentTag());
		else
			transaction.replace(R.id.main_frame, SoundSheetFragment.Companion.getNewInstance(soundSheet), soundSheet.getFragmentTag());

		transaction.commit();
		fragmentManager.executePendingTransactions();

		((ActionbarEditText) this.findViewById(R.id.et_set_label)).setText(soundSheet.getLabel());
	}

	private SoundSheetFragment getCurrentSoundFragment()
	{
		Fragment currentFragment = this.getFragmentManager().findFragmentById(R.id.main_frame);
		if (currentFragment != null && currentFragment.isVisible() && currentFragment instanceof SoundSheetFragment)
			return (SoundSheetFragment) currentFragment;
		return null;
	}

}
