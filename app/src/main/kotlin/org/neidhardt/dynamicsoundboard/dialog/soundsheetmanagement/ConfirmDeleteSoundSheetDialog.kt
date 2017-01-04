package org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement

import android.os.Bundle
import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteSoundSheetDialog : BaseConfirmDeleteDialog() {

	private var fragmentTag: String? = null

	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private val soundManager = SoundboardApplication.newSoundManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG)
	}

	override val infoTextResource: Int = R.string.dialog_confirm_delete_soundsheet_message

	override fun delete() {
		val fragmentTag = this.fragmentTag
		if (fragmentTag != null) {
			this.soundSheetManager.soundSheets.findByFragmentTag(fragmentTag)?.let { soundSheet ->
				val soundsInSoundSheet = this.soundManager.sounds[soundSheet] ?: emptyList()
				this.soundManager.remove(soundSheet, soundsInSoundSheet)
				this.soundSheetManager.remove(listOf(soundSheet))
			}
		}
	}

	companion object {
		private val TAG = ConfirmDeleteSoundSheetDialog::class.java.name

		private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.dialog.soundmanagement.ConfirmDeleteSoundsDialog.fragmentTag"

		fun showInstance(manager: FragmentManager, fragmentTag: String) {
			val dialog = ConfirmDeleteSoundSheetDialog()

			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, fragmentTag)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}
}