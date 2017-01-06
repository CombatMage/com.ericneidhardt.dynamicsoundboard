package org.neidhardt.dynamicsoundboard.dialog

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
object GenericAddDialog {

	fun showAddSoundLayoutDialog(fragmentManager: FragmentManager) {
		val hint = SoundboardApplication.context.getString(R.string.genericadd_SoundLayoutHint)
		val name = SoundboardApplication.soundLayoutManager.suggestedName
		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "AddSoundLayoutDialog",
				editTextConfig = EditTextConfig(hint, name),
				dialogConfig = DialogConfig(R.string.genericrename_SoundLayoutTitle,
						R.string.genericadd_SoundLayoutMessage),
				positiveButton = ButtonConfig(R.string.all_add, { dialog, input ->
					val layout = NewSoundLayout().apply {
						this.isSelected = false
						this.databaseId = SoundLayoutManager.getNewDatabaseIdForLabel(name)
						this.label = name
					}
					SoundboardApplication.soundLayoutManager.add(layout)
				}),
				negativeButton = ButtonConfig(R.string.all_cancel, { dialog, input ->
				})
		)
	}

	fun showAddSoundSheetDialog(fragmentManager: FragmentManager) {
		val hint = SoundboardApplication.context.getString(R.string.genericadd_SoundSheetHint)
		val name = SoundboardApplication.soundSheetManager.suggestedName
		GenericEditTextDialog.showInstance(
				fragmentManager = fragmentManager,
				fragmentTag = "AddSoundSheetDialog",
				editTextConfig = EditTextConfig(hint, name),
				dialogConfig = DialogConfig(R.string.genericrename_SoundSheetTitle,
						R.string.genericadd_SoundSheetMessage),
				positiveButton = ButtonConfig(R.string.all_add, { dialog, input ->
					val manager = SoundboardApplication.soundSheetManager
					val soundSheet = NewSoundSheet().apply {
						this.label = label
						this.fragmentTag = SoundSheetManager.getNewFragmentTagForLabel(label)
					}
					manager.add(soundSheet)
					manager.setSelected(soundSheet)
				}),
				negativeButton = ButtonConfig(R.string.all_cancel, { dialog, input ->
				})
		)
	}
}