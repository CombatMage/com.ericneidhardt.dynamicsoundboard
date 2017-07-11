package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.trello.navi2.Event
import com.trello.navi2.rx.RxNavi
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_base.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.android_utils.EnhancedAppCompatActivity
import org.neidhardt.android_utils.RxEnhancedAppCompatActivity
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.databinding.ActivityBaseBinding
import org.neidhardt.dynamicsoundboard.dialog.GenericAddDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.LoadLayoutDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.StoreLayoutDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundFromIntentDialog
import org.neidhardt.dynamicsoundboard.manager.selectedSoundSheet
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.hasPermissionReadStorage
import org.neidhardt.dynamicsoundboard.misc.hasPermissionWriteStorage
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.viewmodel.ToolbarVM
import org.neidhardt.dynamicsoundboard.soundsheetfragment.SoundSheetFragment
import org.neidhardt.dynamicsoundboard.view_helper.navigationdrawer_helper.NoAnimationDrawerToggle
import org.neidhardt.utils.letThis
import java.util.*

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		EnhancedAppCompatActivity(),
		SoundActivityContract.View {

	private lateinit var binding: ActivityBaseBinding
	private var drawerToggle: ActionBarDrawerToggle? = null

	private val drawerLayout: DrawerLayout? get() = this.drawerlayout_soundactivity // this view does not exists in tablet layout

	private val soundSheetManager = SoundboardApplication.soundSheetManager

	private lateinit var toolbarVM: ToolbarVM
	private lateinit var presenter: SoundActivityContract.Presenter

	init {
		RxNavi.observe(this, Event.CREATE).subscribe {

			this.presenter = SoundActivityPresenter(
					this,
					SoundActivityModel(this.applicationContext, this.soundSheetManager)
			)

			this.toolbarVM = ToolbarVM().letThis {
				it.titleClickedCallback = { this.presenter.userClicksSoundSheetTitle() }
				it.addSoundSheetClickedCallback = { this.presenter.userClicksAddSoundSheet() }
				it.addSoundClickedCallback = { this.presenter.userClicksAddSound() }
				it.addSoundFromDirectoryClickedCallback = { this.presenter.userClicksAddSounds() }
			}

			this.binding = DataBindingUtil.setContentView<ActivityBaseBinding>(
					this, R.layout.activity_base)
			this.binding.layoutToolbar.layoutToolbarContent.viewModel = this.toolbarVM

			val toolbar = this.binding.layoutToolbar.toolbarMain
			if (this.drawerLayout != null) {
				this.drawerToggle = NoAnimationDrawerToggle(
						this,
						this.drawerLayout,
						toolbar,
						this.navigationDrawerFragment)

				this.drawerLayout?.addDrawerListener(this.drawerToggle!!)
			}

			this.appbarlayout_main.setExpanded(true)
			this.setSupportActionBar(toolbar)

			this.volumeControlStream = AudioManager.STREAM_MUSIC

			RxEnhancedAppCompatActivity.receivesIntent(this)
					.bindToLifecycle(this.activityLifeCycle)
					.subscribe { intent ->
						if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
							this.presenter.userOpenSoundFileWithApp(intent.data)
						}
					}
		}

		RxNavi.observe(this, Event.POST_CREATE).subscribe {
			this.presenter.onCreated()
			this.drawerToggle?.syncState()
		}

		RxNavi.observe(this, Event.RESUME).subscribe {
			EventBus.getDefault().postSticky(ActivityStateChangedEvent(true))
			this.presenter.onResumed()
		}

		RxNavi.observe(this, Event.PAUSE).subscribe {
			this.presenter.onPaused()
		}

		RxNavi.observe(this, Event.REQUEST_PERMISSIONS_RESULT).subscribe { result ->
			when (result.requestCode()) {
				IntentRequest.REQUEST_PERMISSIONS -> { this.presenter.onUserHasChangedPermissions() }
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		this.menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		if (this.drawerToggle?.onOptionsItemSelected(item) ?: false)
			return true

		when (item.itemId) {
			R.id.action_load_sound_sheets -> {
				this.presenter.userClicksLoadLayout()
				return true
			}
			R.id.action_store_sound_sheets -> {
				this.presenter.userClicksStoreLayout()
				return true
			}
			R.id.action_preferences -> {
				this.presenter.userClicksPreferences()
				return true
			}
			R.id.action_about -> {
				this.presenter.userClicksInfoAbout()
				return true
			}
			R.id.action_clear_sound_sheets -> {
				this.presenter.userClicksClearSoundSheets()
				return true
			}
			R.id.action_clear_play_list -> {
				this.presenter.userClickClearPlaylist()
				return true
			}
			else -> return false
		}
	}

	// TODO refactor
	private var closeAppOnBackPress = false
	override fun onBackPressed() {
		if (this.drawerLayout?.isDrawerOpen(Gravity.START) == true) { // first close navigation drawer if open
			this.closeNavigationDrawer()
		}
		else if (!this.closeAppOnBackPress) {
			Toast.makeText(this, R.string.soundactivity_ToastCloseAppOnBackPress, Toast.LENGTH_SHORT).show()
			this.closeAppOnBackPress = true
		}
		else {
			super.onBackPressed()
		}
	}

	// TODO refactor
	fun onSoundSheetFragmentResumed() {
		this.toolbarVM.isSoundSheetActionsEnable = true
	}

	// TODO refactor
	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		EventBus.getDefault().postSticky(ActivityStateChangedEvent(false))
	}

	// TODO refactor
	private fun closeNavigationDrawer() {
		this.drawerLayout?.apply {
			if (this.isDrawerOpen(Gravity.START))
				this.closeDrawer(Gravity.START)
		}
	}

	private fun removeSoundFragment(fragment: SoundSheetFragment) {
		val fragmentManager = this.supportFragmentManager
		fragmentManager.beginTransaction().remove(fragment).commit()
		if (fragment.isVisible)
			this.toolbarVM.isSoundSheetActionsEnable = false
		fragmentManager.executePendingTransactions()
	}

	private fun openSoundFragment(soundSheet: SoundSheet?) {
		if (!this.isActivityResumed)
			return

		if (soundSheet == null)
			return

		this.closeNavigationDrawer()

		val fragmentManager = this.supportFragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag) ?:
				SoundSheetFragment.getNewInstance(soundSheet)
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
		get() {
			return this.supportFragmentManager.findFragmentById(R.id.navigation_drawer_fragment)
					as NavigationDrawerFragment
		}

	override fun showSoundSheetActionsInToolbar(show: Boolean) {
		this.toolbarVM.isSoundSheetActionsEnable = show
	}

	override fun updateUiForSoundSheets(soundSheets: List<SoundSheet>) {
		val selectedSoundSheet = soundSheets.selectedSoundSheet
		this.toolbarVM.title = selectedSoundSheet?.label ?: this.getString(R.string.app_name)
		this.showSoundSheetActionsInToolbar(selectedSoundSheet != null)

		val currentFragment = this.currentSoundFragment
		if (currentFragment == null) {
			this.openSoundFragment(selectedSoundSheet)
		}
		else if (selectedSoundSheet == null) {
			this.removeSoundFragment(currentFragment)
		}
		else if (currentFragment.fragmentTag != selectedSoundSheet.fragmentTag) {
			this.openSoundFragment(selectedSoundSheet)
		}
	}

	override fun openRenameSoundSheetDialog() {
		this.soundSheetManager.soundSheets.selectedSoundSheet?.let {
			this.openRenameSoundSheetDialog()
			GenericRenameDialogs.showRenameSoundSheetDialog(this.supportFragmentManager, it)
		}
	}

	override fun openAddSheetDialog() {
		GenericAddDialogs.showAddSoundSheetDialog(this.supportFragmentManager)
	}

	override fun openAddSoundDialog() {
		this.currentSoundFragment?.fragmentTag?.let {
			AddNewSoundDialog.show(this.supportFragmentManager, it)
		}
	}

	override fun openAddSoundDialog(soundUri: Uri, name: String, availableSoundSheets: List<SoundSheet>) {
		AddNewSoundFromIntentDialog.showInstance(
				this.supportFragmentManager,
				soundUri,
				name,
				availableSoundSheets)
	}

	override fun openAddSoundsDialog() {
		this.currentSoundFragment?.fragmentTag?.let {
			AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, it)
		}
	}

	override fun openExplainPermissionReadStorageDialog() {
		this.postAfterOnResume { this.explainReadStoragePermission() }
	}

	override fun openExplainPermissionWriteStorageDialog() {
		this.postAfterOnResume { this.explainWriteStoragePermission() }
	}

	override fun openExplainPermissionReadPhoneStateDialog() {
		this.postAfterOnResume { this.explainReadPhoneStatePermission() }
	}

	override fun requestPermissions(permissions: Array<String>) {
		ActivityCompat.requestPermissions(this, permissions, IntentRequest.REQUEST_PERMISSIONS)
	}

	override fun openLoadLayoutDialog() {
		LoadLayoutDialog.showInstance(this.supportFragmentManager)
	}

	override fun openStoreLayoutDialog() {
		StoreLayoutDialog.showInstance(this.supportFragmentManager)
	}

	override fun openConfirmClearSoundSheetsDialog() {
		GenericConfirmDialogs.showConfirmDeleteAllSoundSheetsDialog(this.supportFragmentManager)
	}

	override fun openConfirmClearPlaylistDialog() {
		GenericConfirmDialogs.showConfirmDeletePlaylistDialog(this.supportFragmentManager)
	}

	override fun openInfoActivity() {
		this.startActivity(Intent(this, AboutActivity::class.java))
		this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing)
	}

	override fun openPreferenceActivity() {
		this.startActivity(Intent(this, PreferenceActivity::class.java))
		this.overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_nothing)
	}

	override fun getMissingPermissions(): Array<String> {
		val requiredPermissions = ArrayList<String>()
		if (!this.hasPermissionReadStorage)
			requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
		if (!this.hasPermissionWriteStorage)
			requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		return requiredPermissions.toTypedArray()
	}
}
