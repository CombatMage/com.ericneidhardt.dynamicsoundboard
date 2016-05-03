package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteAllSoundSheetsDialog : BaseConfirmDeleteDialog()
{
	private val soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess
	private val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	private val soundsDataAccess = SoundboardApplication.soundsDataAccess
	private val soundsDataStorage = SoundboardApplication.soundsDataStorage

	override val infoTextResource: Int = R.string.dialog_confirm_delete_all_soundsheets_message

	override fun delete()
    {
		val soundSheets = this.soundSheetsDataAccess.getSoundSheets()
		this.soundActivity.removeSoundFragments(soundSheets)

		for (soundSheet in soundSheets)
        {
			val soundsInSoundSheet = this.soundsDataAccess.getSoundsInFragment(soundSheet.fragmentTag)
			this.soundsDataStorage.removeSounds(soundsInSoundSheet)
		}
		this.soundSheetsDataStorage.removeSoundSheets(soundSheets)
	}

	companion object
    {
		private val TAG = ConfirmDeleteAllSoundSheetsDialog::class.java.name

		fun showInstance(manager: FragmentManager) {
			val dialog = ConfirmDeleteAllSoundSheetsDialog()
			dialog.show(manager, TAG)
		}
	}
}
