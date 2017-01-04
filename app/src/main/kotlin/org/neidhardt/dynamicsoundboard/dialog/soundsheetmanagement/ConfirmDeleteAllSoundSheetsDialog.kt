package org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement

import android.support.v4.app.FragmentManager
import org.neidhardt.android_utils.misc.getCopyList
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteAllSoundSheetsDialog : BaseConfirmDeleteDialog() {

	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager

	override val infoTextResource: Int = R.string.dialog_confirm_delete_all_soundsheets_message

	override fun delete() {
		this.soundSheetManager.soundSheets.getCopyList().forEach { soundSheet ->
			val soundsInSoundSheet = this.soundManager.sounds[soundSheet] ?: emptyList()
			this.soundManager.remove(soundSheet, soundsInSoundSheet)
			this.soundSheetManager.remove(listOf(soundSheet))
		}
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
