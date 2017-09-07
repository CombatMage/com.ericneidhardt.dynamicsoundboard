package org.neidhardt.dynamicsoundboard.soundactivity

import android.net.Uri
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SoundActivityContract {

	interface View {
		enum class ToolbarState {
			NORMAL,
			SOUND_SHEET_ACTIVE
		}

		var toolbarState: ToolbarState

		fun updateUiForSoundSheets(soundSheets: List<SoundSheet>)
		val isNavigationDrawerOpen: Boolean
		fun closeNavigationDrawer()
		fun finishActivity()
		fun openRenameSoundSheetDialog()
		fun openAddSheetDialog()
		fun openAddSoundDialog()
		fun openAddSoundDialog(soundUri: Uri, name: String, availableSoundSheets: List<SoundSheet>)
		fun openAddSoundsDialog()
		fun openLoadLayoutDialog()
		fun openStoreLayoutDialog()
		fun openConfirmClearSoundSheetsDialog()
		fun openConfirmClearPlaylistDialog()
		fun openInfoActivity()
		fun openPreferenceActivity()
		fun showToastMessage(messageId: Int)
	}
	interface Presenter {
		fun onResumed()
		fun onPaused()
		fun userClicksBackButton()
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
	}
	interface Model {
		fun startNotificationService()
		fun saveData()
		fun loadSoundSheets(): Observable<List<SoundSheet>>
		fun getSoundSheets(): List<SoundSheet>
		fun getNameForNewSoundSheet(): String
	}
}