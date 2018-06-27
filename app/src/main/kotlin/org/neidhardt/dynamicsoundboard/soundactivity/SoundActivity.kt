package org.neidhardt.dynamicsoundboard.soundactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.main.layout_toolbar_content.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.androidutils.EnhancedAppCompatActivity
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
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.preferenceactivity.PreferenceActivity
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundsheetfragment.SoundSheetFragment
import org.neidhardt.dynamicsoundboard.soundactivity.viewhelper.NoAnimationDrawerToggle

/**
 * File created by eric.neidhardt on 29.09.2015.
 */
class SoundActivity :
		EnhancedAppCompatActivity(),
		SoundActivityContract.View {

	// this view does not exists in tablet layout
	private val drawerLayout: DrawerLayout? get() = this.drawerlayout_soundactivity

	private val appDataRepository = SoundboardApplication.appDataRepository
	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
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

		val toolbar = this.toolbar_soundactivity
		this.appTitle = toolbar.tv_layout_toolbar_content_app_name
		this.soundSheetLabel = toolbar.et_layout_toolbar_content_title
		this.buttonAddSoundSheet = toolbar.ib_layout_toolbar_content_add_sound_sheet
		this.buttonAddSound = toolbar.ib_layout_toolbar_content_add_sound
		this.buttonAddSoundsFromDir = toolbar.ib_layout_toolbar_content_add_sound_dir

		this.buttonAddSound.setOnClickListener { this.presenter.userClicksAddSound() }
		this.buttonAddSoundsFromDir.setOnClickListener { this.presenter.userClicksAddSounds() }
		this.buttonAddSoundSheet.setOnClickListener { this.presenter.userClicksAddSoundSheet() }
		this.soundSheetLabel.setOnClickListener { this.presenter.userClicksSoundSheetTitle() }

		this.configureToolbar(toolbar as Toolbar)

		this.presenter = SoundActivityPresenter(
				this,
				SoundActivityModel(
						this.applicationContext,
						this.appDataRepository,
						this.soundLayoutManager,
						this.soundSheetManager))
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

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		this.menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		if (this.drawerToggle?.onOptionsItemSelected(item) == true)
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
		get() {
			return if (this.appTitle.visibility == View.VISIBLE) {
				SoundActivityContract.View.ToolbarState.NORMAL
			} else {
				SoundActivityContract.View.ToolbarState.SOUND_SHEET_ACTIVE
			}
		}
		set(value) {
			if (value == SoundActivityContract.View.ToolbarState.NORMAL) {
				this.buttonAddSound.visibility = View.GONE
				this.buttonAddSoundsFromDir.visibility = View.GONE
				this.soundSheetLabel.visibility = View.GONE
				this.appTitle.visibility = View.VISIBLE
			} else {
				this.buttonAddSound.visibility = View.VISIBLE
				this.buttonAddSoundsFromDir.visibility = View.VISIBLE
				this.soundSheetLabel.visibility = View.VISIBLE
				this.appTitle.visibility = View.GONE
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
		when {
			currentFragment == null -> this.openSoundFragment(selectedSoundSheet)
			selectedSoundSheet == null -> this.removeSoundFragment(currentFragment)
			currentFragment.fragmentTag != selectedSoundSheet.fragmentTag -> this.openSoundFragment(selectedSoundSheet)
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

	override fun showToastMessage(messageId: Int) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
	}
}
