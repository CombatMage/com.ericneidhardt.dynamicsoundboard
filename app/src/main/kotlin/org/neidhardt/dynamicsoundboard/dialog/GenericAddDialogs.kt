package org.neidhardt.dynamicsoundboard.dialog

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dialog.generic.GenericEditTextDialog
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
object GenericAddDialogs {

	fun showAddSoundLayoutDialog(fragmentManager: FragmentManager) {
		val hint = SoundboardApplication.context.getString(R.string.genericadd_SoundLayoutHint)
		val name = SoundboardApplication.soundLayoutManager.suggestedName
		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "AddSoundLayoutDialog",
				editTextConfig = GenericEditTextDialog.EditTextConfig(hint, name),
				dialogConfig = GenericEditTextDialog.DialogConfig(R.string.genericadd_SoundLayoutTitle,
						R.string.genericadd_SoundLayoutMessage),
				positiveButton = GenericEditTextDialog.ButtonConfig(R.string.all_add, { _, input ->
					val layout = SoundLayout().apply {
						this.isSelected = false
						this.databaseId = SoundLayoutManager.getNewDatabaseIdForLabel(input)
						this.label = input
					}
					SoundboardApplication.soundLayoutManager.add(layout)
				}),
				negativeButton = GenericEditTextDialog.ButtonConfig(R.string.all_cancel, { _, _ ->
				})
		)
	}

	fun showAddSoundSheetDialog(fragmentManager: FragmentManager) {
		val hint = SoundboardApplication.context.getString(R.string.genericadd_SoundSheetHint)
		val name = SoundboardApplication.soundSheetManager.suggestedName
		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "AddSoundSheetDialog",
				editTextConfig = GenericEditTextDialog.EditTextConfig(hint, name),
				dialogConfig = GenericEditTextDialog.DialogConfig(R.string.genericrename_SoundSheetTitle,
						R.string.genericadd_SoundSheetMessage),
				positiveButton = GenericEditTextDialog.ButtonConfig(R.string.all_add, { _, input ->
					val manager = SoundboardApplication.soundSheetManager
					val soundSheet = SoundSheet().apply {
						this.label = input
						this.fragmentTag = SoundSheetManager.getNewFragmentTagForLabel(input)
					}
					manager.add(soundSheet)
					manager.setSelected(soundSheet)
				}),
				negativeButton = GenericEditTextDialog.ButtonConfig(R.string.all_cancel, { _, _ ->
				})
		)
	}
}