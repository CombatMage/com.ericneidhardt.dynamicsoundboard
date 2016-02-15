package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views

import android.app.FragmentManager
import android.os.Bundle
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteSoundSheetDialog : BaseConfirmDeleteDialog()
{
	private var fragmentTag: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG)
	}

	override fun getInfoTextResource(): Int = R.string.dialog_confirm_delete_soundsheet_message

	override fun delete()
	{
		val fragmentTag = this.fragmentTag
		if (fragmentTag != null)
		{
			this.soundsDataStorage.removeSounds(this.soundsDataAccess.getSoundsInFragment(fragmentTag))
			val soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag)
			if (soundSheet != null) {
				this.soundSheetsDataStorage.removeSoundSheets(listOf(soundSheet))
				this.soundActivity.removeSoundFragment(soundSheet)
			}
		}
	}

	companion object
	{
		private val TAG = ConfirmDeleteSoundSheetDialog::class.java.name

		private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog.fragmentTag"

		fun showInstance(manager: FragmentManager, fragmentTag: String)
		{
			val dialog = ConfirmDeleteSoundSheetDialog()

			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, fragmentTag)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}
}
