package org.neidhardt.dynamicsoundboard.dialog

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.generic.GenericEditTextDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.RenameSoundFileDialog
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
object GenericRenameDialogs {

	fun showRenameSoundDialog(fragmentManager: FragmentManager, playerData: MediaPlayerData) {
		val label = playerData.label ?: ""
		val hint = SoundboardApplication.context
				.getString(R.string.all_CurrentNameIs)
				.replace("{STR}",label)

		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "RenameSoundDialog",
				dialogConfig = GenericEditTextDialog.DialogConfig(R.string.genericrename_SoundTitle, 0),
				editTextConfig = GenericEditTextDialog.EditTextConfig(hint, label),
				positiveButton = GenericEditTextDialog.ButtonConfig(R.string.all_rename, { _, input ->
					val currentLabel = playerData.label
					if (currentLabel != input) {
						playerData.label = input
						RenameSoundFileDialog.show(fragmentManager, playerData)
					}
				}),
				negativeButton = GenericEditTextDialog.ButtonConfig(R.string.all_cancel, { _, _ ->
				})
		)
	}

	fun showRenameSoundSheetDialog(fragmentManager: FragmentManager, soundSheet: SoundSheet) {
		val label = soundSheet.label ?: ""
		val hint = SoundboardApplication.context
				.getString(R.string.all_CurrentNameIs)
				.replace("{STR}",label)

		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "RenameSoundSheetDialog",
				dialogConfig = GenericEditTextDialog.DialogConfig(R.string.genericrename_SoundSheetTitle, 0),
				editTextConfig = GenericEditTextDialog.EditTextConfig(hint, label),
				positiveButton = GenericEditTextDialog.ButtonConfig(R.string.all_rename, { _, input ->
					soundSheet.label = input
					SoundboardApplication.soundSheetManager.notifyHasChanged(soundSheet)
				}),
				negativeButton = GenericEditTextDialog.ButtonConfig(R.string.all_cancel, { _, _ ->
				})
		)
	}

	fun showRenameSoundLayoutDialog(fragmentManager: FragmentManager, soundLayout: SoundLayout) {
		val label = soundLayout.label ?: ""

		val hint = SoundboardApplication.context
				.getString(R.string.all_CurrentNameIs)
				.replace("{STR}",label)

		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "RenameSoundLayoutDialog",
				dialogConfig = GenericEditTextDialog.DialogConfig(R.string.genericrename_SoundLayoutTitle, 0),
				editTextConfig = GenericEditTextDialog.EditTextConfig(hint, label),
				positiveButton = GenericEditTextDialog.ButtonConfig(R.string.all_rename, { _, input ->
					soundLayout.label = input
					SoundboardApplication.soundLayoutManager.notifyHasChanged(soundLayout)
				}),
				negativeButton = GenericEditTextDialog.ButtonConfig(R.string.all_cancel, { _, _ -> })
		)

	}
}