package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 04.09.2017.
 */
class NavigationDrawerFragmentModel(
		private val soundSheetManager: SoundSheetManager
) : NavigationDrawerFragmentContract.Model {

	override fun setSoundSheetSelected(soundSheet: SoundSheet) {
		this.soundSheetManager.setSelected(soundSheet)
	}
}