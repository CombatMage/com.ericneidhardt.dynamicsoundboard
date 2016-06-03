package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

import android.app.Dialog
import android.os.Bundle
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.views.BaseDialog

/**
 * File created by eric.neidhardt on 12.04.2015.
 */
abstract class SoundSettingsBaseDialog : BaseDialog()
{
	private val soundsDataAccess = SoundboardApplication.soundsDataAccess

	protected abstract var fragmentTag: String
	protected abstract var player: MediaPlayerController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val args = this.arguments
		if (args != null) {
			val playerId = args.getString(KEY_PLAYER_ID)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG)
			this.player = this.soundsDataAccess.getSoundById(this.fragmentTag, playerId) as MediaPlayerController
		}
	}

	abstract override fun onCreateDialog(savedInstanceState: Bundle?): Dialog

	companion object {
		private val KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.soundmanagement.dialog.SoundSettingsBaseDialog.playerId"
		private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.dialog.SoundSettingsBaseDialog.fragmentTag"

		fun addArguments(dialog: SoundSettingsBaseDialog, playerId: String, fragmentTag: String) {
			val args = Bundle()
			args.putString(KEY_PLAYER_ID, playerId)
			args.putString(KEY_FRAGMENT_TAG, fragmentTag)

			dialog.arguments = args
		}
	}
}
