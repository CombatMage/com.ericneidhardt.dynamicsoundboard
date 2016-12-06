package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * Created by eric.neidhardt on 06.12.2016.
 */
interface ISoundLayoutManager {

	val soundLayouts: List<SoundLayout>

	fun addSoundLayout(soundLayout: SoundLayout)

	fun updateSoundLayout(update: () -> SoundLayout): SoundLayout

	fun setSoundLayoutSelected(soundLayout: SoundLayout)

	fun removeSoundLayouts(soundLayouts: List<SoundLayout>)

	fun getSuggestedName(): String
}

val List<SoundLayout>.selectedLayout: SoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<SoundLayout>.activeLayout: SoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")

fun List<SoundLayout>.findById(databaseId: String): SoundLayout?
	= this.firstOrNull { it.databaseId == databaseId }