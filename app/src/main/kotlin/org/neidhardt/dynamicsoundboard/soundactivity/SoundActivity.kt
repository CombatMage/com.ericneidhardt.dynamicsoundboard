package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.fileexplorer.LoadLayoutDialog
import org.neidhardt.dynamicsoundboard.fileexplorer.StoreLayoutDialog
import org.neidhardt.dynamicsoundboard.introduction.IntroductionFragment
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OnActionModeChangeRequestedEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnOpenSoundLayoutSettingsEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnSoundLayoutSelectedEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.notifications.service.NotificationService
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteAllSoundSheetsDialog
import org.neidhardt.dynamicsoundboard.views.edittext.ActionbarEditText
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent
import java.util.*

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
public class SoundActivity :
		AppCompatActivity(),
		View.OnClickListener,
		CustomEditText.OnTextEditedListener,
		RequestPermissionHelper,
		OnActionModeChangeRequestedEventListener,
		OnSoundLayoutSelectedEventListener,
		OnOpenSoundLayoutSettingsEvent,
		OnSoundSheetOpenEventListener,
		OnSoundSheetsInitEventLisenter,
		OnSoundSheetsChangedEventListener
{
	private val TAG = javaClass.name

	public var isActivityVisible = true
	public var isActionModeActive = false

	private var navigationDrawerLayout: DrawerLayout? = null
	private var drawerToggle: ActionBarDrawerToggle? = null
	private var actionMode: android.support.v7.view.ActionMode? = null

	private var phoneStateListener: PauseSoundOnCallListener = PauseSoundOnCallListener()

	private val eventBus = EventBus.getDefault()

	private val soundsDataAccess = DynamicSoundboardApplication.getSoundsDataAccess()
	private val soundsDataStorage = DynamicSoundboardApplication.getSoundsDataStorage()
	private val soundsDataUtil = DynamicSoundboardApplication.getSoundsDataUtil()

	private val soundSheetsDataAccess = DynamicSoundboardApplication.getSoundSheetsDataAccess()
	private val soundSheetsDataUtil = DynamicSoundboardApplication.getSoundSheetsDataUtil()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_base)

		if (!this.requestPermissionsReadStorageIfRequired())
		{
			this.soundsDataUtil.initIfRequired()
			this.soundSheetsDataUtil.initIfRequired()
		}
		this.requestPermissionsWriteStorageIfRequired()
		this.requestPermissionsReadPhoneStateIfRequired()

		this.initToolbar()
		this.initNavigationDrawer()
		this.openIntroductionFragmentIfRequired()

		this.volumeControlStream = AudioManager.STREAM_MUSIC
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode)
		{
			IntentRequest.REQUEST_PERMISSION_READ_STORAGE -> {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					this.soundsDataUtil.initIfRequired()
					this.soundSheetsDataUtil.initIfRequired()
				} else
					this.finish()
			}
			IntentRequest.REQUEST_PERMISSION_WRITE_STORAGE -> {
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
					this.finish()
			}
			IntentRequest.REQUEST_PERMISSION_READ_PHONE_STATE -> {
			} // nothing to be done
		}
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)
		this.handleIntent(intent)
	}

	public fun handleIntent(intent: Intent?)
	{
		if (intent == null)
			return

		if (intent.action == Intent.ACTION_VIEW && intent.data != null)
		{
			if (this.soundSheetsDataAccess.getSoundSheets().size == 0)
				AddNewSoundFromIntent.showInstance(this.fragmentManager, intent.data,
						this.soundSheetsDataUtil.getSuggestedName(), null)
			else
				AddNewSoundFromIntent.showInstance(this.fragmentManager, intent.data,
						this.soundSheetsDataUtil.getSuggestedName(), soundSheetsDataAccess.getSoundSheets())
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean
	{
		this.menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
	}

	private fun initToolbar()
	{
		val appBarLayout = this.findViewById(R.id.appBarLayout) as AppBarLayout
		appBarLayout.setExpanded(true);

		val toolbar = this.findViewById(R.id.toolbar) as Toolbar
		this.setSupportActionBar(toolbar)

		this.findViewById(R.id.tv_app_name).visibility = View.VISIBLE
		this.findViewById(R.id.action_add_sound_sheet).setOnClickListener(this)

		(this.findViewById(R.id.et_set_label) as ActionbarEditText).apply {
			visibility = View.GONE
			onTextEditedListener = this@SoundActivity
			getCurrentSoundFragment()?.apply {
				soundSheetsDataAccess.getSoundSheetForFragmentTag(this.fragmentTag)?.apply {
					text = this.label
				}
			}
		}
	}

	private fun initNavigationDrawer()
	{
		// The navigation drawer is fixed on tablets in landscape mode, therefore we need to check the Views type
		val navigationDrawerLayout = this.findViewById(R.id.root_layout)
		if (navigationDrawerLayout != null && navigationDrawerLayout is DrawerLayout)
		{
			this.navigationDrawerLayout = navigationDrawerLayout
			this.drawerToggle = object : ActionBarDrawerToggle(this,
					this.navigationDrawerLayout,
					this.findViewById(R.id.toolbar) as Toolbar,
					R.string.navigation_drawer_content_description_open,
					R.string.navigation_drawer_content_description_close)
			{

				// override onDrawerSlide and pass 0 to super disable arrow animation
				override fun onDrawerSlide(drawerView: View, slideOffset: Float)
				{
					super.onDrawerSlide(drawerView, 0f)
				}
			}

			this.drawerToggle?.isDrawerIndicatorEnabled = true
			this.navigationDrawerLayout?.setDrawerListener(drawerToggle)
		}
	}

	override fun onPostCreate(savedInstanceState: Bundle?)
	{
		super.onPostCreate(savedInstanceState)
		this.drawerToggle?.syncState()
	}

	override fun onWindowFocusChanged(hasFocus: Boolean)
	{
		super.onWindowFocusChanged(hasFocus)
		this.getNavigationDrawerFragment().adjustViewPagerToContent()
	}

	override fun onStart()
	{
		super.onStart()
		this.eventBus.registerSticky(this)
	}

	override fun onResume()
	{
		super.onResume()

		this.registerPauseSoundOnCallListener(this.phoneStateListener)

		this.startService(Intent(this.applicationContext, NotificationService::class.java))

		this.isActivityVisible = true
		this.eventBus.postSticky(ActivityStateChangedEvent(true))

		this.setSoundSheetActionsEnable(false)

		this.soundsDataUtil.initIfRequired()
		if (this.soundSheetsDataUtil.initIfRequired())
			this.onSoundSheetsInit()
	}

	private fun onSoundSheetsInit()
    {
		this.handleIntent(this.intent) // sound sheets have been loaded, check if there is pending intent to handle
		this.openSoundFragment(this.soundSheetsDataAccess.getSelectedItem())
	}

	override fun onPause()
    {
		super.onPause()
		this.isActivityVisible = false
		this.unregisterPauseSoundOnCallListener(this.phoneStateListener)
	}

	override fun onUserLeaveHint()
    {
		super.onUserLeaveHint()
		this.eventBus.postSticky(ActivityStateChangedEvent(false))
	}

	override fun onStop()
    {
		this.eventBus.unregister(this)

		if (this.isFinishing)
        {
			// we remove all loaded sounds, which have no corresponding SoundSheet
			val fragmentsWithLoadedSounds = this.soundsDataAccess.sounds.keys
			val fragmentsWithLoadedSoundsToRemove = HashSet<String>()

			for (fragmentTag in fragmentsWithLoadedSounds) {
				if (this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag) == null) // no sound sheet exists
					fragmentsWithLoadedSoundsToRemove.add(fragmentTag)
			}
			for (fragmentTag in fragmentsWithLoadedSoundsToRemove)
				this.soundsDataStorage.removeSounds(this.soundsDataAccess.getSoundsInFragment(fragmentTag))
		}

		super.onStop()
	}

	public fun setSoundSheetActionsEnable(enable: Boolean)
    {
		var viewState = if (enable) View.VISIBLE else View.GONE
		this.findViewById(R.id.action_add_sound).visibility = viewState
		this.findViewById(R.id.action_add_sound_dir).visibility = viewState
		this.findViewById(R.id.et_set_label).visibility = viewState

		viewState = if (!enable) View.VISIBLE else View.GONE
		this.findViewById(R.id.tv_app_name).visibility = viewState
	}

	override fun onEvent(event: SoundLayoutSelectedEvent)
    {
		this.removeSoundFragments(this.soundSheetsDataAccess.getSoundSheets())
		this.setSoundSheetActionsEnable(false)
		this.soundSheetsDataUtil.initIfRequired()

		this.soundsDataUtil.releaseAll()
		this.soundsDataUtil.initIfRequired()
	}

	override fun onEvent(event: OpenSoundLayoutSettingsEvent)
	{
		SoundLayoutSettingsDialog.showInstance(this.fragmentManager, event.soundLayout.databaseId)
	}

	override fun onEvent(event: OpenSoundSheetEvent)
	{
		this.openSoundFragment(event.soundSheetToOpen)
	}

	override fun onEvent(event: SoundSheetsInitEvent)
	{
		this.onSoundSheetsInit()
	}

	override fun onEventMainThread(event: SoundSheetsRemovedEvent)
	{
		val removedSoundSheets = event.soundSheets
		this.removeSoundFragments(removedSoundSheets)

		if (this.soundSheetsDataAccess.getSoundSheets().size == 0)
			this.setSoundSheetActionsEnable(false)
	}

	override fun onEventMainThread(event: SoundSheetAddedEvent) {}

	override fun onEventMainThread(event: SoundSheetChangedEvent) {}

	override fun onEvent(event: ActionModeChangeRequestedEvent)
	{
		val requestedAction = event.requestedAction
		if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.START)
		{
			this.actionMode = this.startSupportActionMode(event.actionModeCallback)
			return
		}
		if (this.actionMode != null)
		{
			if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.STOP)
			{
				this.actionMode?.finish()
				this.actionMode = null
			}
			else if (requestedAction == ActionModeChangeRequestedEvent.REQUEST.INVALIDATE)
				this.actionMode?.invalidate()
		}
	}

	/**
	 * This is called by greenRobot EventBus in case a the floating action button was clicked
	 * @param event delivered FabClickedEvent
	 */
	@SuppressWarnings("unused")
	public fun onEvent(event: FabClickedEvent)
	{
		Logger.d(TAG, "onEvent: " + event)

		val soundSheetFragment = this.getCurrentSoundFragment()
		val currentlyPlayingSounds = this.soundsDataAccess.currentlyPlayingSounds

		if (currentlyPlayingSounds.size > 0)
		{
			val copyCurrentlyPlayingSounds = ArrayList<MediaPlayerController>(currentlyPlayingSounds.size)
			copyCurrentlyPlayingSounds.addAll(currentlyPlayingSounds)
			for (sound in copyCurrentlyPlayingSounds)
				sound.pauseSound()
		}
		else if (soundSheetFragment == null)
		{
			AddNewSoundSheetDialog.showInstance(this.fragmentManager, this.soundSheetsDataUtil.getSuggestedName())
		}
		else
		{
			if (SoundboardPreferences.useSystemBrowserForFiles())
			{
				val intent = Intent(Intent.ACTION_GET_CONTENT)
				intent.setType(FileUtils.MIME_AUDIO)
				soundSheetFragment.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
			}
			else
			{
				val currentSoundSheet = this.getCurrentSoundFragment()
				if (currentSoundSheet != null)
					AddNewSoundFromDirectoryDialog.showInstance(this.fragmentManager, currentSoundSheet.fragmentTag)
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case creating a new sound failed.
	 * @param event delivered CreatingPlayerFailedEvent
	 */
	@SuppressWarnings("unused")
	public fun onEventMainThread(event: CreatingPlayerFailedEvent) {
		val message = resources.getString(R.string.music_service_loading_sound_failed) + " " + FileUtils.getFileNameFromUri(applicationContext, event.failingPlayerData.uri)
		Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean
	{
		super.onOptionsItemSelected(item)
		if (this.drawerToggle?.onOptionsItemSelected(item) ?: false)
			return true

		when (item?.itemId)
		{
			R.id.action_load_sound_sheets -> {
				LoadLayoutDialog.showInstance(this.fragmentManager)
				return true
			}
			R.id.action_store_sound_sheets -> {
				StoreLayoutDialog.showInstance(this.fragmentManager)
				return true
			}
			R.id.action_preferences -> {
				this.startActivity(Intent(this, PreferenceActivity::class.java))
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing)
				return true
			}
			R.id.action_about -> {
				this.startActivity(Intent(this, AboutActivity::class.java))
				this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing)
				return true
			}
			R.id.action_clear_sound_sheets -> {
				ConfirmDeleteAllSoundSheetsDialog.showInstance(this.fragmentManager)
				return true
			}
			else -> return false
		}
	}

	override fun onClick(view: View)
	{
		when (view.id)
		{
			R.id.action_add_sound_sheet -> AddNewSoundSheetDialog.showInstance(this.fragmentManager, this.soundSheetsDataUtil.getSuggestedName())
			else -> Logger.e(TAG, "unknown item clicked " + view)
		}
	}

	override fun onTextEdited(text: String)
	{
		val currentSoundSheetFragment = this.getCurrentSoundFragment()
		if (currentSoundSheetFragment != null)
		{
			val soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(currentSoundSheetFragment.fragmentTag)
			if (soundSheet != null)
			{
				soundSheet.label = text
				soundSheet.updateItemInDatabaseAsync()
				this.eventBus.post(SoundSheetChangedEvent(soundSheet))
			}
		}
	}

	override fun onSupportActionModeStarted(mode: ActionMode?)
	{
		super.onSupportActionModeStarted(mode)
		this.isActionModeActive = true
	}

	override fun onSupportActionModeFinished(mode: ActionMode?)
	{
		super.onSupportActionModeFinished(mode)
		this.isActionModeActive = false
	}

	@SuppressWarnings("ResourceType") // for unknown reason, inspection demand using Gravity.START, but this would lead to warnings
	fun closeNavigationDrawer()
	{
		if (this.navigationDrawerLayout == null)
			return
		if (this.navigationDrawerLayout!!.isDrawerOpen(Gravity.START))
			this.navigationDrawerLayout!!.closeDrawer(Gravity.START)
	}

	public fun getNavigationDrawerFragment(): NavigationDrawerFragment {
		return this.fragmentManager.findFragmentById(R.id.navigation_drawer_fragment) as NavigationDrawerFragment
	}

	public fun removeSoundFragments(soundSheets: List<SoundSheet>?)
	{
		if (soundSheets == null || soundSheets.size == 0)
			return

		val fragmentManager = this.fragmentManager
		for (soundSheet in soundSheets)
		{
			val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag)
			if (fragment != null)
				fragmentManager.beginTransaction().remove(fragment).commit()
		}
		fragmentManager.executePendingTransactions()

		if (this.soundSheetsDataAccess.getSoundSheets().size == 0)
			this.setSoundSheetActionsEnable(false)
	}

	public fun removeSoundFragment(soundSheet: SoundSheet)
	{
		val fragmentManager = this.fragmentManager
		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag)
		if (fragment != null)
		{
			fragmentManager.beginTransaction().remove(fragment).commit()
			if (fragment.isVisible)
				this.setSoundSheetActionsEnable(false)
		}
		fragmentManager.executePendingTransactions()
	}

	public fun openIntroductionFragmentIfRequired()
	{
		if (!this.isActivityVisible)
			return

		if (this.getCurrentSoundFragment() != null)
			return

		val fragmentManager = this.fragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(IntroductionFragment.TAG) ?: IntroductionFragment()
		transaction.replace(R.id.main_frame, fragment, IntroductionFragment.TAG)

		transaction.commit()
		fragmentManager.executePendingTransactions()
	}

	public fun openSoundFragment(soundSheet: SoundSheet?)
	{
		if (!this.isActivityVisible)
			return

		if (soundSheet == null)
			return

		this.closeNavigationDrawer()

		val fragmentManager = this.fragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag) ?: getNewInstance(soundSheet)
		transaction.replace(R.id.main_frame, fragment, soundSheet.fragmentTag)

		transaction.commit()
		fragmentManager.executePendingTransactions()

		(this.findViewById(R.id.et_set_label) as ActionbarEditText).text = soundSheet.label
	}

	private fun getCurrentSoundFragment(): SoundSheetFragment?
	{
		val currentFragment = this.fragmentManager.findFragmentById(R.id.main_frame)
		if (currentFragment != null && currentFragment is SoundSheetFragment)
			return currentFragment
		return null
	}

}
