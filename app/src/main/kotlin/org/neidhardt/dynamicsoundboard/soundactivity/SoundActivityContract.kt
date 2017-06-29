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
		fun openAddSoundsDialog()
	}
	interface Presenter {
		fun onResumed()
		fun onPaused()
		fun userClicksSoundSheetTitle()
		fun userClicksAddSoundSheet()
		fun userClicksAddSoundDialog()
		fun userClicksAddSoundsDialog()
		fun userOpenSoundFileWithApp(soundUri: Uri)
	}
	interface Model {
		fun saveData()
		fun getSoundSheets(): List<SoundSheet>
		fun getNameForNewSoundSheet(): String
	}
}