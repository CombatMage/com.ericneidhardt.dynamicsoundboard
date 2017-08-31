package org.neidhardt.dynamicsoundboard.dialog

import org.neidhardt.dynamicsoundboard.R
import android.support.v4.app.FragmentManager
import org.neidhardt.utils.getCopyList
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.generic.GenericConfirmDialog
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 09.01.2017.
 */
object GenericConfirmDialogs {

	fun showConfirmDeleteSoundsDialog(fragmentManager: FragmentManager, soundSheet: SoundSheet) {
		GenericConfirmDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "ConfirmDeleteSoundsDialog",
				dialogConfig = GenericConfirmDialog.DialogConfig(0, R.string.genericconfirm_DeleteSoundsMessage),
				positiveButton = GenericConfirmDialog.ButtonConfig(R.string.all_delete, {
					val soundManager = SoundboardApplication.soundManager
					soundManager.remove(soundSheet, soundManager.sounds[soundSheet] ?: emptyList())
				}),
				negativeButton = GenericConfirmDialog.ButtonConfig(R.string.all_cancel, {
				})
		)
	}

	fun showConfirmDeletePlaylistDialog(fragmentManager: FragmentManager) {
		GenericConfirmDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "ConfirmDeleteSoundsDialog",
				dialogConfig = GenericConfirmDialog.DialogConfig(0, R.string.genericconfirm_DeletePlaylistMessage),
				positiveButton = GenericConfirmDialog.ButtonConfig(R.string.all_delete, {
					val playlistManager = SoundboardApplication.playlistManager
					val playlist = playlistManager.playlist
					playlistManager.remove(playlist)
				}),
				negativeButton = GenericConfirmDialog.ButtonConfig(R.string.all_cancel, {
				})
		)
	}

	fun showConfirmDeleteAllSoundSheetsDialog(fragmentManager: FragmentManager) {
		GenericConfirmDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "ConfirmDeleteSoundsDialog",
				dialogConfig = GenericConfirmDialog.DialogConfig(0, R.string.genericconfirm_DeleteAllSoundSheetsMessage),
				positiveButton = GenericConfirmDialog.ButtonConfig(R.string.all_delete, {
					val soundManager = SoundboardApplication.soundManager
					val soundSheetManager = SoundboardApplication.soundSheetManager
					soundSheetManager.soundSheets.getCopyList().forEach { soundSheet ->
						val soundsInSoundSheet = soundManager.sounds[soundSheet] ?: emptyList()
						soundManager.remove(soundSheet, soundsInSoundSheet)
						soundSheetManager.remove(listOf(soundSheet))
					}
				}),
				negativeButton = GenericConfirmDialog.ButtonConfig(R.string.all_cancel, {
				})
		)
	}

	fun showConfirmDeleteSoundSheetDialog(fragmentManager: FragmentManager, soundSheet: SoundSheet) {
		GenericConfirmDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "ConfirmDeleteSoundsDialog",
				dialogConfig = GenericConfirmDialog.DialogConfig(0, R.string.genericconfirm_DeleteSoundSheetMessage),
				positiveButton = GenericConfirmDialog.ButtonConfig(R.string.all_delete, {
					val soundManager = SoundboardApplication.soundManager
					val soundSheetManager = SoundboardApplication.soundSheetManager
					val soundsInSoundSheet = soundManager.sounds[soundSheet] ?: emptyList()
					soundManager.remove(soundSheet, soundsInSoundSheet)
					soundSheetManager.remove(listOf(soundSheet))
				}),
				negativeButton = GenericConfirmDialog.ButtonConfig(R.string.all_cancel, {
				})
		)
	}
}