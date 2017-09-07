package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.net.Uri
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityPresenter(
		private val view: SoundActivityContract.View,
		private val model: SoundActivityContract.Model
) : SoundActivityContract.Presenter {

	override fun onCreated() {
		val missingPermissions = this.view.getMissingPermissions()
		this.view.requestPermissions(missingPermissions)
	}

	override fun onResumed() {
		this.view.updateUiForSoundSheets(emptyList())

		this.model.startNotificationService()
		this.model.loadSoundSheets()
				.subscribe { this.view.updateUiForSoundSheets(it) }
	}

	override fun onPaused() {
		this.model.saveData()
	}

	private var closeAppOnBackPress = false
	override fun userClicksBackButton() {
		if (this.view.isNavigationDrawerOpen) { // first close navigation drawer if open
			this.view.closeNavigationDrawer()
		}
		else if (!this.closeAppOnBackPress) {
			this.view.showToastMessage(R.string.soundactivity_ToastCloseAppOnBackPress)
			this.closeAppOnBackPress = true
		}
		else {
			this.view.finishActivity()
		}
	}

	override fun userClicksSoundSheetTitle() {
		this.view.openRenameSoundSheetDialog()
	}

	override fun userClicksAddSoundSheet() {
		this.view.openAddSheetDialog()
	}

	override fun userClicksAddSound() {
		this.view.openAddSoundDialog()
	}

	override fun userClicksAddSounds() {
		this.view.openAddSoundsDialog()
	}

	override fun userClicksLoadLayout() {
		this.view.openLoadLayoutDialog()
	}

	override fun userClicksStoreLayout() {
		this.view.openStoreLayoutDialog()
	}

	override fun userClicksPreferences() {
		this.view.openPreferenceActivity()
	}

	override fun userClicksInfoAbout() {
		this.view.openInfoActivity()
	}

	override fun userClicksClearSoundSheets() {
		this.view.openConfirmClearSoundSheetsDialog()
	}

	override fun userClickClearPlaylist() {
		this.view.openConfirmClearPlaylistDialog()
	}

	override fun userOpenSoundFileWithApp(soundUri: Uri) {
		val suggestedName = this.model.getNameForNewSoundSheet()
		val soundSheets = this.model.getSoundSheets()
		this.view.openAddSoundDialog(soundUri, suggestedName, soundSheets)
	}

	override fun onUserHasChangedPermissions() {
		val missingPermissions = this.view.getMissingPermissions()
		if (missingPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
			this.view.openExplainPermissionReadStorageDialog()
		if (missingPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			this.view.openExplainPermissionWriteStorageDialog()
		if (missingPermissions.contains(Manifest.permission.READ_PHONE_STATE))
			this.view.openExplainPermissionReadPhoneStateDialog()
	}
}