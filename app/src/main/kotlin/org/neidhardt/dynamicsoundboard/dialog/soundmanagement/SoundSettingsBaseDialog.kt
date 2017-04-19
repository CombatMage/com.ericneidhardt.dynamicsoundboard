package org.neidhardt.dynamicsoundboard.dialog.soundmanagement

import android.app.Dialog
import android.os.Bundle
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.manager.findById
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

/**
 * File created by eric.neidhardt on 12.04.2015.
 */
abstract class SoundSettingsBaseDialog : BaseDialog() {

	protected val soundSheetManager = SoundboardApplication.soundSheetManager
	protected val soundManager = SoundboardApplication.soundManager
	protected val playlistManager = SoundboardApplication.playlistManager

	protected abstract var fragmentTag: String
	protected abstract var soundSheet: NewSoundSheet? // can be null if sound is from playlist
	protected abstract var player: MediaPlayerController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val args = this.arguments
		if (args != null) {
			val playerId = args.getString(KEY_PLAYER_ID)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG)
			this.soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(fragmentTag)

			this.player = this.soundManager.sounds[this.soundSheet]?.findById(playerId)
					?: this.playlistManager.playlist.findById(playerId)
					?: throw IllegalArgumentException("no player found for given id")
		}
	}

	abstract override fun onCreateDialog(savedInstanceState: Bundle?): Dialog

	companion object {
		private val KEY_PLAYER_ID = "SoundSettingsBaseDialog.playerId"
		private val KEY_FRAGMENT_TAG = "SoundSettingsBaseDialog.fragmentTag"

		fun addArguments(dialog: SoundSettingsBaseDialog, playerId: String, fragmentTag: String) {
			val args = Bundle()
			args.putString(KEY_PLAYER_ID, playerId)
			args.putString(KEY_FRAGMENT_TAG, fragmentTag)

			dialog.arguments = args
		}
	}
}
