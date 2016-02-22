package org.neidhardt.dynamicsoundboard.soundmanagement.views

import android.app.FragmentManager
import android.os.Bundle
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
class ConfirmDeleteSoundsDialog : BaseConfirmDeleteDialog() {

	private var fragmentTag: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG)
	}

	override val infoTextResource: Int = R.string.dialog_confirm_delete_sounds_in_soundheet_message

	override fun delete() {
		this.soundsDataStorage.removeSounds(this.soundsDataAccess.getSoundsInFragment(this.fragmentTag as String))
	}

	companion object {
		private val TAG = ConfirmDeleteSoundsDialog::class.java.name

		private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog.fragmentTag"

		fun showInstance(manager: FragmentManager, fragmentTag: String) {
			val dialog = ConfirmDeleteSoundsDialog()

			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, fragmentTag)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}
}
