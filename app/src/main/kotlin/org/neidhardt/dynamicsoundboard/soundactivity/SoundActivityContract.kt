package org.neidhardt.dynamicsoundboard.soundactivity

import android.net.Uri
import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SoundActivityContract {
	interface View {
		fun openRenameSoundSheetDialog()
		fun openAddSheetDialog()
		fun openAddSoundDialog()
		fun openAddSoundDialog(soundUri: Uri, name: String, availableSoundSheets: List<SoundSheet>)
		fun openAddSoundsDialog()
		fun openExplainPermissionReadStorageDialog()
		fun openExplainPermissionWriteStorageDialog()
		fun openExplainPermissionReadPhoneStateDialog()
		fun requestPermissions(permissions: Array<String>)
		fun getMissingPermissions(): Array<String>
	}
	interface Presenter {
		fun onResumed()
		fun onPaused()
		fun userClicksSoundSheetTitle()
		fun userClicksAddSoundSheet()
		fun userClicksAddSoundDialog()
		fun userClicksAddSoundsDialog()
		fun userOpenSoundFileWithApp(soundUri: Uri)
		fun onPermissionsHaveChanged()
	}
	interface Model {
		fun saveData()
		fun getSoundSheets(): List<SoundSheet>
		fun getNameForNewSoundSheet(): String
	}
}