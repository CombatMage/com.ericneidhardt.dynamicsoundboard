package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.layout_toolbar_content.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.android_utils.EnhancedAppCompatActivity
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.GenericAddDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.LoadLayoutDialog
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.StoreLayoutDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundFromIntentDialog
import org.neidhardt.dynamicsoundboard.infoactivity.InfoActivity
import org.neidhardt.dynamicsoundboard.manager.selectedSoundSheet
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.hasPermissionReadStorage
import org.neidhardt.dynamicsoundboard.misc.hasPermissionWriteStorage
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.preferenceactivity.PreferenceActivity
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.viewhelper.explainReadPhoneStatePermission
import org.neidhardt.dynamicsoundboard.soundactivity.viewhelper.explainReadStoragePermission
import org.neidhardt.dynamicsoundboard.soundactivity.viewhelper.explainWriteStoragePermission
import org.neidhardt.dynamicsoundboard.soundsheetfragment.SoundSheetFragment
import org.neidhardt.dynamicsoundboard.viewhelper.navigationdrawer_helper.NoAnimationDrawerToggle
import java.util.*

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		EnhancedAppCompatActivity(),
		SoundActivityContract.View {

	// this view does not exists in tablet layout
	private val drawerLayout: DrawerLayout? get() = this.drawerlayout_soundactivity
	private val soundSheetManager = SoundboardApplication.soundSheetManager

	private var drawerToggle: ActionBarDrawerToggle? = null

	private lateinit var appTitle: TextView
	private lateinit var soundSheetLabel: TextView
	private lateinit var buttonAddSoundSheet: View
	private lateinit var buttonAddSound: View
	private lateinit var buttonAddSoundsFromDir: View

	private lateinit var presenter: SoundActivityContract.Presenter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_base)

		this.volumeControlStream = AudioManager.STREAM_MUSIC

		val toolbar = this.toolbar_main
		this.appTitle = toolbar.tv_layout_toolbar_content_app_name
		this.soundSheetLabel = toolbar.et_layout_toolbar_content_title
		this.buttonAddSoundSheet = toolbar.ib_layout_toolbar_content_add_sound_sheet
		this.buttonAddSound = toolbar.ib_layout_toolbar_content_add_sound
		this.buttonAddSoundsFromDir = toolbar.ib_layout_toolbar_content_add_sound_dir

		this.buttonAddSound.setOnClickListener { this.presenter.userClicksAddSound() }
		this.buttonAddSoundsFromDir.setOnClickListener { this.presenter.userClicksAddSounds() }
		this.buttonAddSoundSheet.setOnClickListener { this.presenter.userClicksAddSoundSheet() }
		this.soundSheetLabel.setOnClickListener { this.presenter.userClicksSoundSheetTitle() }

		this.configureToolbar(toolbar)

		this.presenter = SoundActivityPresenter(
				this,
				SoundActivityModel(
						this.applicationContext,
						this.soundSheetManager)
		)

		this.presenter.onCreated()
	}

	private fun configureToolbar(toolbar: Toolbar) {
		if (this.drawerLayout != null) {
			this.drawerToggle = NoAnimationDrawerToggle(
					this,
					this.drawerLayout,
					toolbar)

			this.drawerLayout?.addDrawerListener(this.drawerToggle!!)
		}

		this.appbarlayout_main.setExpanded(true)
		this.setSupportActionBar(toolbar)
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		this.drawerToggle?.syncState()

		this.onNewIntent(this.intent)
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent == null) return
		if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
			this.presenter.userOpenSoundFileWithApp(intent.data)
		}
	}

	override fun onResume() {
		super.onResume()
		EventBus.getDefault().postSticky(ActivityStateChangedEvent(true))
		this.presenter.onResumed()
	}

	override fun onPause() {
		super.onPause()
		this.presenter.onPaused()
	}

	override fun onRequestPermissionsResult(
			requestCode: Int,
			permissions: Array<out String>,
			grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			IntentRequest.REQUEST_PERMISSIONS -> { this.presenter.onUserHasChangedPermissions() }
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

	@SuppressLint("MissingSuperCall")
	override fun onBackPressed() {
		this.presenter.userClicksBackButton()
	}

	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		EventBus.getDefault().postSticky(ActivityStateChangedEvent(false))
	}

	override fun closeNavigationDrawer() {
		this.drawerLayout?.apply {
			if (isNavigationDrawerOpen)
				this.closeDrawer(Gravity.START)
		}
	}

	override var toolbarState: SoundActivityContract.View.ToolbarState
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
		set(value) {
			if (value == SoundActivityContract.View.ToolbarState.NORMAL) {
				this.buttonAddSound.visibility = View.GONE
				this.buttonAddSoundsFromDir.visibility = View.GONE
				this.soundSheetLabel.visibility = View.GONE
				this.appTitle.visibility = View.VISIBLE
			} else {

			}
		}


	override val isNavigationDrawerOpen: Boolean
		get() = this.drawerLayout?.isDrawerOpen(Gravity.START) == true

	override fun finishActivity() {
		this.finish()
	}

	private val currentSoundFragment: SoundSheetFragment?
		get() {
			val currentFragment = this.supportFragmentManager.findFragmentById(R.id.main_frame)
			if (currentFragment != null && currentFragment is SoundSheetFragment)
				return currentFragment
			return null
		}

	override fun updateUiForSoundSheets(soundSheets: List<SoundSheet>) {
		val selectedSoundSheet = soundSheets.selectedSoundSheet
		if (selectedSoundSheet != null) {
			this.toolbarState = SoundActivityContract.View.ToolbarState.SOUND_SHEET_ACTIVE
			this.soundSheetLabel.text = selectedSoundSheet.label
		} else {
			this.toolbarState = SoundActivityContract.View.ToolbarState.NORMAL
		}

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

	private fun openSoundFragment(soundSheet: SoundSheet?) {
		if (!this.isActivityResumed)
			return

		if (soundSheet == null)
			return

		val fragmentManager = this.supportFragmentManager
		val transaction = fragmentManager.beginTransaction()

		val fragment = fragmentManager.findFragmentByTag(soundSheet.fragmentTag) ?:
				SoundSheetFragment.getNewInstance(soundSheet)
		transaction.replace(R.id.main_frame, fragment, soundSheet.fragmentTag)

		transaction.commit()
		fragmentManager.executePendingTransactions()
	}

	private fun removeSoundFragment(fragment: SoundSheetFragment) {
		val fragmentManager = this.supportFragmentManager
		fragmentManager.beginTransaction().remove(fragment).commit()
		if (fragment.isVisible) {
			this.toolbarState = SoundActivityContract.View.ToolbarState.NORMAL
		}
		fragmentManager.executePendingTransactions()
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
		if (permissions.isNotEmpty()) {
			ActivityCompat.requestPermissions(this, permissions, IntentRequest.REQUEST_PERMISSIONS)
		}
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
		this.startActivity(Intent(this, InfoActivity::class.java))
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

	override fun showToastMessage(messageId: Int) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
	}
}
