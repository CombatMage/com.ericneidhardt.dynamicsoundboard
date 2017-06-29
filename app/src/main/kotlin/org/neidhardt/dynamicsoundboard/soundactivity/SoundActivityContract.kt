package org.neidhardt.dynamicsoundboard.soundactivity

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
	}
	interface Model {
		fun saveData()
	}
}