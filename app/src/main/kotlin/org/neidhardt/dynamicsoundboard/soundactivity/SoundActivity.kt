package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.layout_toolbar_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.databinding.ActivityBaseBinding
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.fileexplorer.LoadLayoutDialog
import org.neidhardt.dynamicsoundboard.fileexplorer.StoreLayoutDialog
import org.neidhardt.dynamicsoundboard.introduction.IntroductionFragment
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnOpenSoundLayoutSettingsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent
import org.neidhardt.dynamicsoundboard.notifications.NotificationService
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.viewmodel.ToolbarVM
import org.neidhardt.dynamicsoundboard.soundcontrol.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.OnSoundLayoutsChangedEventListener
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundFromIntentDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.ConfirmDeletePlayListDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteAllSoundSheetsDialog
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent
import org.neidhardt.ui_utils.views.CustomEditText
import org.neidhardt.utils.letThis
import org.neidhardt.utils.registerIfRequired
import java.util.*
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		BaseActivity(),
		RequestPermissionHelper,
		OnSoundLayoutsChangedEventListener,
		OnOpenSoundLayoutSettingsEventListener,
		OnSoundSheetOpenEventListener,
		OnSoundSheetsInitEventLisenter,
		OnSoundSheetsChangedEventListener
{
	private val TAG = javaClass.name

	private val phoneStateListener: PauseSoundOnCallListener = PauseSoundOnCallListener()

	private val eventBus = EventBus.getDefault()

	private val soundsDataAccess = SoundboardApplication.soundsDataAccess
	private val soundsDataStorage = SoundboardApplication.soundsDataStorage
	private val soundsDataUtil = SoundboardApplication.soundsDataUtil

	private val soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess
	private val soundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil

	private var binding by Delegates.notNull<ActivityBaseBinding>()
	private val toolbar by lazy { this.binding.layoutToolbar.tbMain }
	private val toolbarTitle by lazy { this.et_layout_toolbar_content_title }

	val toolbarVM = ToolbarVM().letThis {
		it.onTitleChanged = { text -> this.setNewSoundSheetTitle(text) }
		it.onAddSoundSheetClicked = { AddNewSoundSheetDialog.showInstance(this.supportFragmentManager, this.soundSheetsDataUtil.getSuggestedName()) }
		it.onAddSoundClicked = { this.currentSoundFragment?.fragmentTag?.let { AddNewSoundDialog(this.supportFragmentManager, it) } }
		it.onAddSoundFromDirectoryClicked = { this.currentSoundFragment?.fragmentTag?.let { AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, it) } }
	}

	private val navigationDrawerLayout: DrawerLayout? by lazy { this.dl_main } // this view does not exists in tablet layout
	private val drawerToggle: ActionBarDrawerToggle? by lazy {
		if (this.navigationDrawerLayout != null) {
			object : ActionBarDrawerToggle(
					this,
					this.navigationDrawerLayout,
					this.toolbar,
					R.string.navigation_drawer_content_description_open,
					R.string.navigation_drawer_content_description_close)
			{
				// override onDrawerSlide and pass 0 to super disable arrow animation
				override fun onDrawerSlide(drawerView: View, slideOffset: Float) { super.onDrawerSlide(drawerView, 0f) }
			}.apply {
				this@SoundActivity.navigationDrawerLayout?.addDrawerListener(this)
				this.isDrawerIndicatorEnabled = true
			}
		} else null
	}
	private val isNavigationDrawerOpen: Boolean get() = this.navigationDrawerLayout?.isDrawerOpen(Gravity.START) ?: false

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.binding = DataBindingUtil.setContentView<ActivityBaseBinding>(this, R.layout.activity_base).letThis {
			it.layoutToolbar.layoutToolbarContent.viewModel = this.toolbarVM
		}

		val requestedPermissions = this.requestPermissionsIfRequired()
		if (!requestedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) &&
				!requestedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			this.soundsDataUtil.initIfRequired()
			this.soundSheetsDataUtil.initIfRequired()
		}

		this.initToolbar()
		this.openIntroductionFragmentIfRequired()

		this.volumeControlStream = AudioManager.STREAM_MUSIC
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode)
		{
			IntentRequest.REQUEST_PERMISSIONS -> {
				this.postAfterOnResume { if (!this.hasPermissionReadStorage) this.explainReadStoragePermission() }
				this.postAfterOnResume { if (!this.hasPermissionWriteStorage) this.explainWriteStoragePermission() }
				this.postAfterOnResume { if (!this.hasPermissionPhoneState) this.explainReadPhoneStatePermission() }

				if (this.hasPermissionWriteStorage && this.hasPermissionReadStorage)
				{
					this.soundsDataUtil.initIfRequired()
					this.soundSheetsDataUtil.initIfRequired()
				}
			}
		}
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)
		this.handleIntent(intent)
	}

	fun handleIntent(intent: Intent?)
	{
		if (intent == null)
			return

		if (intent.action == Intent.ACTION_VIEW && intent.data != null)
		{
			if (this.soundSheetsDataAccess.getSoundSheets().size == 0)
				AddNewSoundFromIntentDialog.showInstance(this.supportFragmentManager, intent.data,
						this.soundSheetsDataUtil.getSuggestedName(), null)
			else
				AddNewSoundFromIntentDialog.showInstance(this.supportFragmentManager, intent.data,
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
		this.binding.ablMain.setExpanded(true)

		this.setSupportActionBar(this.toolbar)

		val currentSoundSheet = this.currentSoundFragment
		if (currentSoundSheet != null) {
			val currentLabel = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(currentSoundSheet.fragmentTag)?.label
			this.toolbarVM.title = currentLabel
		}
	}

	override fun onPostCreate(savedInstanceState: Bundle?)
	{
		super.onPostCreate(savedInstanceState)
		this.drawerToggle?.syncState()
	}

	override fun onStart()
	{
		super.onStart()
		this.eventBus.registerIfRequired(this)
	}

	override fun onResume()
	{
		super.onResume()

		this.registerPauseSoundOnCallListener(this.phoneStateListener)
		NotificationService.start(this)
		this.eventBus.postSticky(ActivityStateChangedEvent(true))

		this.toolbarVM.isSoundSheetActionsEnable = false

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

	override fun onBackPressed()
	{
		if (this.isNavigationDrawerOpen)
			this.closeNavigationDrawer()
		else
			super.onBackPressed()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent)
	{
		this.removeSoundFragments(this.soundSheetsDataAccess.getSoundSheets())
		this.toolbarVM.isSoundSheetActionsEnable = false

		this.soundSheetsDataUtil.releaseAll()
		this.soundSheetsDataUtil.initIfRequired()

		this.soundsDataUtil.releaseAll()
		this.soundsDataUtil.initIfRequired()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutSettingsEvent)
	{
		SoundLayoutSettingsDialog.showInstance(this.supportFragmentManager, event.soundLayout.databaseId)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundSheetEvent)
	{
		this.openSoundFragment(event.soundSheetToOpen)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundSheetsInitEvent)
	{
		this.onSoundSheetsInit()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundSheetsRemovedEvent)
	{
		val removedSoundSheets = event.soundSheets
		this.removeSoundFragments(removedSoundSheets)

		if (this.soundSheetsDataAccess.getSoundSheets().size == 0)
			this.toolbarVM.isSoundSheetActionsEnable = false
	}

	/**
	 * This is called by greenRobot EventBus in case a the floating action button was clicked
	 * @param event delivered FabClickedEvent
	 */
	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onEvent(event: FabClickedEvent)
	{
		Logger.d(TAG, "onEvent: " + event)

		val soundSheetFragment = this.currentSoundFragment
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
			AddNewSoundSheetDialog.showInstance(this.supportFragmentManager, this.soundSheetsDataUtil.getSuggestedName())
		}
		else
		{
			if (SoundboardPreferences.useSystemBrowserForFiles())
			{
				val intent = Intent(Intent.ACTION_GET_CONTENT)
				intent.type = FileUtils.MIME_AUDIO
				soundSheetFragment.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
			}
			else
			{
				val currentSoundSheet = this.currentSoundFragment
				if (currentSoundSheet != null)
					AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, currentSoundSheet.fragmentTag)
			}
		}
	}

	/**
	 * This is called by greenRobot EventBus in case creating a new sound failed.
	 * @param event delivered CreatingPlayerFailedEvent
	 */
	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onEvent(event: CreatingPlayerFailedEvent) {
		val message = resources.getString(R.string.music_service_loading_sound_failed) + " " + FileUtils.getFileNameFromUri(applicationContext, event.failingPlayerData.uri)
		Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		super.onOptionsItemSelected(item)
		if (this.drawerToggle?.onOptionsItemSelected(item) ?: false)
			return true

		when (item.itemId)
		{
			R.id.action_load_sound_sheets -> {
				LoadLayoutDialog.showInstance(this.supportFragmentManager)
				return true
			}
			R.id.action_store_sound_sheets -> {
				StoreLayoutDialog.showInstance(this.supportFragmentManager)
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
				ConfirmDeleteAllSoundSheetsDialog.showInstance(this.supportFragmentManager)
				return true
			}
			R.id.action_clear_play_list -> {
				ConfirmDeletePlayListDialog.showInstance(this.supportFragmentManager)
				return true
			}
			else -> return false
		}
	}

	fun setNewSoundSheetTitle(text: String)
	{
		val currentSoundSheetFragment = this.currentSoundFragment
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

	fun closeNavigationDrawer()
	{
		this.navigationDrawerLayout?.apply {
			if (this.isDrawerOpen(Gravity.START))
				this.closeDrawer(Gravity.START)
		}
	}

	fun removeSoundFragments(soundSheets: List<SoundSheet>?)
	{
		if (soundSheets == null || soundSheets.size == 0)
			return

		val fragmentManager = this.supportFragmentManager
		for (soundSheet in soundSheets)
		{
			val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag)
			if (fragment != null)
				fragmentManager.beginTransaction().remove(fragment).commit()
		}
		fragmentManager.executePendingTransactions()

		if (this.soundSheetsDataAccess.getSoundSheets().size == 0) 
		{
			this.toolbarVM.isSoundSheetActionsEnable = false
			this.openIntroductionFragmentIfRequired()
		}
	}

	fun removeSoundFragment(soundSheet: SoundSheet)
	{
		val fragmentManager = this.supportFragmentManager
		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag)
		if (fragment != null)
		{
			fragmentManager.beginTransaction().remove(fragment).commit()
			if (fragment.isVisible)
				this.toolbarVM.isSoundSheetActionsEnable = false
		}
		fragmentManager.executePendingTransactions()
	}

	fun openIntroductionFragmentIfRequired()
	{
		if (!this.isActivityResumed)
			return

		if (this.currentSoundFragment != null)
			return

		val fragmentManager = this.supportFragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(IntroductionFragment.TAG) ?: IntroductionFragment()
		transaction.replace(R.id.main_frame, fragment, IntroductionFragment.TAG)

		transaction.commit()
		fragmentManager.executePendingTransactions()
	}

	fun openSoundFragment(soundSheet: SoundSheet?)
	{
		if (!this.isActivityResumed)
			return

		if (soundSheet == null)
			return

		this.closeNavigationDrawer()

		val fragmentManager = this.supportFragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag) ?: getNewInstance(soundSheet)
		transaction.replace(R.id.main_frame, fragment, soundSheet.fragmentTag)

		transaction.commit()
		fragmentManager.executePendingTransactions()

		this.toolbarTitle.text = soundSheet.label
	}

	private val currentSoundFragment: SoundSheetFragment?
		get() {
			val currentFragment = this.supportFragmentManager.findFragmentById(R.id.main_frame)
			if (currentFragment != null && currentFragment is SoundSheetFragment)
				return currentFragment
			return null
		}

	// unused
	override fun onEvent(event: SoundLayoutsRemovedEvent) {}
	override fun onEvent(event: SoundLayoutRenamedEvent) {}
	override fun onEvent(event: SoundSheetAddedEvent) {}
	override fun onEvent(event: SoundSheetChangedEvent) {}
}
