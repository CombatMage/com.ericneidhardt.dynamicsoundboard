package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.trello.navi2.Event
import com.trello.navi2.rx.RxNavi
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
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
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.selectedSoundSheet
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.notifications.NotificationService
import org.neidhardt.dynamicsoundboard.persistance.SaveDataIntentService
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.preferences.AboutActivity
import org.neidhardt.dynamicsoundboard.preferences.PreferenceActivity
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.viewmodel.ToolbarVM
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment
import org.neidhardt.dynamicsoundboard.view_helper.navigationdrawer_helper.NoAnimationDrawerToggle
import org.neidhardt.utils.letThis

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		EnhancedAppCompatActivity(),
		RequestPermissionHelper {

	private lateinit var binding: ActivityBaseBinding
	private var drawerToggle: ActionBarDrawerToggle? = null
	private var toolbarVM: ToolbarVM

	private val toolbar: Toolbar get() = this.binding.layoutToolbar.tbMain
	private val drawerLayout: DrawerLayout? get() = this.drawerlayout_soundactivity // this view does not exists in tablet layout

	init {
		this.toolbarVM = ToolbarVM().letThis {
			it.titleClickedCallback = {
				this.soundSheetManager.soundSheets.selectedSoundSheet?.let {
					GenericRenameDialogs.showRenameSoundSheetDialog(this.supportFragmentManager, it)
				}
			}
			it.addSoundSheetClickedCallback = {
				GenericAddDialogs.showAddSoundSheetDialog(this.supportFragmentManager)
			}
			it.addSoundClickedCallback = {
				this.currentSoundFragment?.fragmentTag?.let {
					AddNewSoundDialog.show(this.supportFragmentManager, it)
				}
			}
			it.addSoundFromDirectoryClickedCallback = {
				this.currentSoundFragment?.fragmentTag?.let {
					AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, it)
				}
			}
		}

		RxNavi.observe(this, Event.CREATE).subscribe {
			this.binding = DataBindingUtil.setContentView<ActivityBaseBinding>(this, R.layout.activity_base).letThis {
				it.layoutToolbar.layoutToolbarContent.viewModel = this.toolbarVM
			}
			this.binding.ablMain.setExpanded(true)
			this.setSupportActionBar(this.toolbar)

			this.requestPermissionsIfRequired()
			this.volumeControlStream = AudioManager.STREAM_MUSIC

			if (this.drawerLayout != null) {
				this.drawerToggle = NoAnimationDrawerToggle(
						this, this.drawerLayout,
						this.toolbar,
						this.navigationDrawerFragment)

				this.drawerLayout?.addDrawerListener(this.drawerToggle!!)
			}

			RxEnhancedAppCompatActivity.receivesIntent(this)
					.bindToLifecycle(this.activityLifeCycle)
					.subscribe { this.handleIntent(it) }
		}

		RxNavi.observe(this, Event.POST_CREATE).subscribe {
			this.drawerToggle?.syncState()
		}
	}

	private val soundSheetManager = SoundboardApplication.soundSheetManager

	private val isNavigationDrawerOpen: Boolean get() = this.drawerLayout?.isDrawerOpen(Gravity.START) ?: false

	private var closeAppOnBackPress = false

	private fun handleIntent(intent: Intent?) {
		if (intent == null)
			return

		if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
			val suggestedName = this.soundSheetManager.suggestedName
			val soundSheets = this.soundSheetManager.soundSheets
			AddNewSoundFromIntentDialog.showInstance(this.supportFragmentManager, intent.data, suggestedName, soundSheets)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			IntentRequest.REQUEST_PERMISSIONS -> {
				this.postAfterOnResume { if (!this.hasPermissionReadStorage) this.explainReadStoragePermission() }
				this.postAfterOnResume { if (!this.hasPermissionWriteStorage) this.explainWriteStoragePermission() }
				//this.postAfterOnResume { if (!this.hasPermissionPhoneState) this.explainReadPhoneStatePermission() }
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		this.menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
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
		else if (selectedSoundSheet == null) {
			this.removeSoundFragment(currentFragment)
		}
		else if (currentFragment.fragmentTag != selectedSoundSheet.fragmentTag) {
			this.openSoundFragment(selectedSoundSheet)
		}
	}

	override fun onResume() {
		super.onResume()

		this.closeAppOnBackPress = false
		this.toolbarVM.isSoundSheetActionsEnable = false

		NotificationService.start(this)
		EventBus.getDefault().postSticky(ActivityStateChangedEvent(true))

		RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
				.bindToLifecycle(this.activityLifeCycle)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.setStateForSoundSheets() }
	}

	override fun onPause() {
		super.onPause()
		SaveDataIntentService.writeBack(this)
	}

	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		EventBus.getDefault().postSticky(ActivityStateChangedEvent(false))
	}

	override fun onBackPressed() {
		if (this.isNavigationDrawerOpen)
			this.closeNavigationDrawer()
		else {
			val backStackSize = this.supportFragmentManager.backStackEntryCount
			if (backStackSize == 0 && !this.closeAppOnBackPress) {
				this.closeAppOnBackPress = true
				Toast.makeText(this, R.string.soundactivity_ToastCloseAppOnBackPress, Toast.LENGTH_SHORT).show()
			}
			else
				super.onBackPressed()
		}
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
				GenericConfirmDialogs.showConfirmDeleteAllSoundSheetsDialog(this.supportFragmentManager)
				return true
			}
			R.id.action_clear_play_list -> {
				GenericConfirmDialogs.showConfirmDeletePlaylistDialog(this.supportFragmentManager)
				return true
			}
			else -> return false
		}
	}

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

	private fun openSoundFragment(soundSheet: NewSoundSheet?) {
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

	fun onSoundSheetFragmentResumed() {
		this.toolbarVM.isSoundSheetActionsEnable = true
	}
}
