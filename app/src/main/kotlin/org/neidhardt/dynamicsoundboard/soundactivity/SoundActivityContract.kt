package org.neidhardt.dynamicsoundboard.soundactivity

import android.net.Uri
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SoundActivityContract {
	interface View {
		fun updateUiForSoundSheets(soundSheets: List<SoundSheet>)
		fun showSoundSheetActionsInToolbar(show: Boolean)
		fun openRenameSoundSheetDialog()
		fun openAddSheetDialog()
		fun openAddSoundDialog()
		fun openAddSoundDialog(soundUri: Uri, name: String, availableSoundSheets: List<SoundSheet>)
		fun openAddSoundsDialog()
		fun openLoadLayoutDialog()
		fun openStoreLayoutDialog()
		fun openConfirmClearSoundSheetsDialog()
		fun openConfirmClearPlaylistDialog()
		fun openExplainPermissionReadStorageDialog()
		fun openExplainPermissionWriteStorageDialog()
		fun openExplainPermissionReadPhoneStateDialog()
		fun openInfoActivity()
		fun openPreferenceActivity()
		fun requestPermissions(permissions: Array<String>)
		fun getMissingPermissions(): Array<String>
	}
	interface Presenter {
		fun onCreated()
		fun onResumed()
		fun onPaused()
		fun userClicksSoundSheetTitle()
		fun userClicksAddSoundSheet()
		fun userClicksAddSound()
		fun userClicksAddSounds()
		fun userClicksLoadLayout()
		fun userClicksStoreLayout()
		fun userClicksPreferences()
		fun userClicksInfoAbout()
		fun userClicksClearSoundSheets()
		fun userClickClearPlaylist()
		fun userOpenSoundFileWithApp(soundUri: Uri)
		fun onUserHasChangedPermissions()
	}
	interface Model {
		fun saveData()
		fun loadSoundSheets(): Observable<List<SoundSheet>>
		fun getSoundSheets(): List<SoundSheet>
		fun getNameForNewSoundSheet(): String
	}
}