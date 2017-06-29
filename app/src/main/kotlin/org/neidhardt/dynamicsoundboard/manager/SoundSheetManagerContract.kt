package org.neidhardt.dynamicsoundboard.manager

import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SoundSheetManagerContract {
	interface Model {
		val soundSheets: List<SoundSheet>
		val suggestedName: String
	}
}