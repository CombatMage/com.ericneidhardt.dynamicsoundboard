package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views

import android.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteAllSoundSheetsDialog : BaseConfirmDeleteDialog()
{
	override fun getInfoTextResource(): Int = R.string.dialog_confirm_delete_all_soundsheets_message

	override fun delete()
    {
		val soundSheets = this.soundSheetsDataAccess.getSoundSheets()
		this.getSoundActivity().removeSoundFragments(soundSheets)

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
