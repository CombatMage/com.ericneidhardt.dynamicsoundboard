package org.neidhardt.dynamicsoundboard.manager

import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class NewSoundSheetManager {

	var onSoundSheetsChangedListener: ((List<NewSoundLayout>) -> Unit)? = null

}