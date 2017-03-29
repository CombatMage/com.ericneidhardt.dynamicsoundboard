package org.neidhardt.dynamicsoundboard.dialog

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.generic.GenericEditTextDialog
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
object GenericRenameDialogs {

	fun showRenameSoundSheetDialog(fragmentManager: FragmentManager, soundSheet: NewSoundSheet) {
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

	fun showRenameSoundLayoutDialog(fragmentManager: FragmentManager, soundLayout: NewSoundLayout) {
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