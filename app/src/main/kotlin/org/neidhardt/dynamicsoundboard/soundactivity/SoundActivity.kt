package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_base.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.misc.getCopyList
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseActivity
import org.neidhardt.dynamicsoundboard.databinding.ActivityBaseBinding
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.LoadLayoutDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.StoreLayoutDialog
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.selectedSoundSheet
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OnOpenSoundLayoutSettingsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent
import org.neidhardt.dynamicsoundboard.notifications.NotificationService
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.viewmodel.ToolbarVM
import org.neidhardt.dynamicsoundboard.soundcontrol.*
import org.neidhardt.dynamicsoundboard.dialog.soundlayoutmanagement.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundFromIntentDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.ConfirmDeletePlayListDialog
import org.neidhardt.dynamicsoundboard.manager.CreatingPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement.AddNewSoundSheetDialog
import org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement.ConfirmDeleteAllSoundSheetsDialog
import org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement.RenameSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButtonView
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.FabClickedEvent
import org.neidhardt.eventbus_utils.registerIfRequired
import org.neidhardt.utils.letThis
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		BaseActivity(),
		RequestPermissionHelper,
		AddPauseFloatingActionButtonView.FabEventListener,
		OnOpenSoundLayoutSettingsEventListener {

	private val phoneStateListener: PauseSoundOnCallListener = PauseSoundOnCallListener()

	private val eventBus = EventBus.getDefault()
	private var subscriptions = CompositeSubscription()

	private val storage = SoundboardApplication.storage
	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private val soundLayoutManager = SoundboardApplication.newSoundLayoutManager

	private var binding by Delegates.notNull<ActivityBaseBinding>()
	private val toolbar by lazy { this.binding.layoutToolbar.tbMain }

	val toolbarVM = ToolbarVM().letThis {
		it.titleClickedCallback = { RenameSoundSheetDialog.showInstance(this.supportFragmentManager) }
		it.addSoundSheetClickedCallback = { AddNewSoundSheetDialog.showInstance(this.supportFragmentManager) }
		it.addSoundClickedCallback = { this.currentSoundFragment?.fragmentTag?.let { AddNewSoundDialog.show(this.supportFragmentManager, it) } }
		it.addSoundFromDirectoryClickedCallback = { this.currentSoundFragment?.fragmentTag?.let { AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, it) } }
	}

	private val navigationDrawerLayout: DrawerLayout? by lazy { this.dl_main } // this view does not exists in tablet layout
	private val drawerToggle: ActionBarDrawerToggle? by lazy {
		if (this.navigationDrawerLayout != null) {
			NoAnimationDrawerToggle(this, this.navigationDrawerLayout, this.toolbar, this.navigationDrawerFragment).letThis {
				this.navigationDrawerLayout?.addDrawerListener(it)
			}
		}
		else null
	}
	private val isNavigationDrawerOpen: Boolean get() = this.navigationDrawerLayout?.isDrawerOpen(Gravity.START) ?: false

	private var closeAppOnBackPress = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.binding = DataBindingUtil.setContentView<ActivityBaseBinding>(this, R.layout.activity_base).letThis {
			it.layoutToolbar.layoutToolbarContent.viewModel = this.toolbarVM
		}
		this.binding.ablMain.setExpanded(true)
		this.setSupportActionBar(this.toolbar)

		this.requestPermissionsIfRequired()
		this.volumeControlStream = AudioManager.STREAM_MUSIC
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		this.drawerToggle?.syncState()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			IntentRequest.REQUEST_PERMISSIONS -> {
				this.postAfterOnResume { if (!this.hasPermissionReadStorage) this.explainReadStoragePermission() }
				this.postAfterOnResume { if (!this.hasPermissionWriteStorage) this.explainWriteStoragePermission() }
				this.postAfterOnResume { if (!this.hasPermissionPhoneState) this.explainReadPhoneStatePermission() }
			}
		}
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)

		this.subscriptions.add(RxNewSoundLayoutManager.completesLoading(this.soundLayoutManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.handleIntent(intent) })
	}

	fun handleIntent(intent: Intent?) {
		if (intent == null)
			return

		if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
			val suggestedName = this.soundSheetManager.suggestedName
			val soundSheets = this.soundSheetManager.soundSheets
			AddNewSoundFromIntentDialog.showInstance(this.supportFragmentManager, intent.data, suggestedName, soundSheets)
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		this.menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
	}

	override fun onStart() {
		super.onStart()
		this.eventBus.registerIfRequired(this)

		this.setStateForSoundSheets()
		this.subscriptions = CompositeSubscription()

		this.subscriptions.add(RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { soundSheets -> this.setStateForSoundSheets() })
	}

	private fun setStateForSoundSheets() {
		val soundSheets = this.soundSheetManager.soundSheets
		val selectedSoundSheet = soundSheets.selectedSoundSheet
		this.toolbarVM.title = selectedSoundSheet?.label ?: this.getString(R.string.app_name)
		this.toolbarVM.isSoundSheetActionsEnable = selectedSoundSheet != null

		val currentFragment = this.currentSoundFragment
		if (currentFragment == null) {
			this.openSoundFragment(selectedSoundSheet)
		}
		else if (currentFragment.fragmentTag != selectedSoundSheet?.fragmentTag) {
			this.openSoundFragment(selectedSoundSheet)
		}
	}

	override fun onResume() {
		super.onResume()

		this.registerPauseSoundOnCallListener(this.phoneStateListener)
		NotificationService.start(this)
		this.eventBus.postSticky(ActivityStateChangedEvent(true))

		this.closeAppOnBackPress = false
		this.toolbarVM.isSoundSheetActionsEnable = false
	}

	override fun onPause() {
		super.onPause()
		this.unregisterPauseSoundOnCallListener(this.phoneStateListener)

		this.storage.save(this.soundLayoutManager.soundLayouts).subscribe()
	}

	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		this.eventBus.postSticky(ActivityStateChangedEvent(false))
	}

	override fun onStop() {
		this.eventBus.unregister(this)
		this.subscriptions.unsubscribe()
		super.onStop()
	}

	override fun onBackPressed() {
		if (this.isNavigationDrawerOpen)
			this.closeNavigationDrawer()
		else {
			val backStackSize = this.supportFragmentManager.backStackEntryCount
			if (backStackSize == 0 && !this.closeAppOnBackPress) {
				this.closeAppOnBackPress = true
				Toast.makeText(this, R.string.toast_close_app_on_back_press, Toast.LENGTH_SHORT).show()
			}
			else
				super.onBackPressed()
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutSettingsEvent) {
		SoundLayoutSettingsDialog.showInstance(this.supportFragmentManager, event.soundLayout.databaseId)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onFabClickedEvent(event: FabClickedEvent) {
		val currentlyPlayingSounds = this.soundLayoutManager.currentlyPlayingSounds
		val currentSoundSheet = this.currentSoundFragment
		if (currentlyPlayingSounds.isNotEmpty()) {
			val copyCurrentlyPlayingSounds = currentlyPlayingSounds.getCopyList()
			for (sound in copyCurrentlyPlayingSounds)
				sound.pauseSound()
		}
		else if (currentSoundSheet == null) {
			AddNewSoundSheetDialog.showInstance(this.supportFragmentManager)
		} else {
			if (SoundboardPreferences.useSystemBrowserForFiles()) {
				val intent = Intent(Intent.ACTION_GET_CONTENT)
				intent.type = FileUtils.MIME_AUDIO
				currentSoundSheet.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
			} else {
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

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		if (this.drawerToggle?.onOptionsItemSelected(item) ?: false)
			return true

		when (item.itemId) {
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

	fun closeNavigationDrawer() {
		this.navigationDrawerLayout?.apply {
			if (this.isDrawerOpen(Gravity.START))
				this.closeDrawer(Gravity.START)
		}
	}

	fun removeSoundFragment(fragment: SoundSheetFragment) {
		val fragmentManager = this.supportFragmentManager
		fragmentManager.beginTransaction().remove(fragment).commit()
		if (fragment.isVisible)
			this.toolbarVM.isSoundSheetActionsEnable = false
		fragmentManager.executePendingTransactions()
	}

	fun openSoundFragment(soundSheet: NewSoundSheet?) {
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
	}

	private val currentSoundFragment: SoundSheetFragment?
		get() {
			val currentFragment = this.supportFragmentManager.findFragmentById(R.id.main_frame)
			if (currentFragment != null && currentFragment is SoundSheetFragment)
				return currentFragment
			return null
		}

	private val navigationDrawerFragment: NavigationDrawerFragment
		get() = this.supportFragmentManager.findFragmentById(R.id.navigation_drawer_fragment) as NavigationDrawerFragment
}

private class NoAnimationDrawerToggle(
		activity: AppCompatActivity,
		drawerLayout: DrawerLayout?,
		toolbar: Toolbar,
		private val navigationDrawer: NavigationDrawerFragment)
: ActionBarDrawerToggle(
		activity,
		drawerLayout,
		toolbar,
		R.string.navigation_drawer_content_description_open,
		R.string.navigation_drawer_content_description_close
) {
	init { this.isDrawerIndicatorEnabled = true }

	// override onDrawerSlide and pass 0 to super disable arrow animation
	override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
		super.onDrawerSlide(drawerView, 0f)
	}

	override fun onDrawerClosed(drawerView: View?) {
		super.onDrawerClosed(drawerView)
		this.navigationDrawer.onNavigationDrawerClosed()
	}
}