package org.neidhardt.dynamicsoundboard.dialog

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
object RenameDialogs {

	fun showRenameSoundSheetDialog(fragmentManager: FragmentManager, soundSheet: NewSoundSheet) {
		val hint = SoundboardApplication.context.getString(R.string.all_CurrentNameIs).replace("{STR}",soundSheet.label)
		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "RenameSoundSheetDialog",
				dialogConfig = DialogConfig(R.string.genericrename_SoundSheetTitle, 0),
				editTextConfig = EditTextConfig(hint, soundSheet.label),
				positiveButton = ButtonConfig(R.string.all_rename, { dialog,input ->
					soundSheet.label = input
					SoundboardApplication.soundSheetManager.notifyHasChanged(soundSheet)
				}),
				negativeButton = ButtonConfig(R.string.all_cancel, { dialog,input ->
				})
		)
	}
}