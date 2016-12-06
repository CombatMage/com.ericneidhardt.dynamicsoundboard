package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * Created by eric.neidhardt on 06.12.2016.
 */
interface ISoundLayoutManager {

	val soundLayouts: List<SoundLayout>

	fun addSoundLayout(soundLayout: SoundLayout)

	fun updateSoundLayout(update: () -> SoundLayout)

	fun removeSoundLayout(soundLayout: SoundLayout)

	fun getSuggestedName(): String
}

val List<SoundLayout>.selectedLayout: SoundLayout
	get() = this.firstOrNull { it.isSelected } ?: throw IllegalStateException("No sound layout is selected")

fun List<SoundLayout>.findByid(databaseId: String): SoundLayout?
	= this.firstOrNull { it.databaseId == databaseId }