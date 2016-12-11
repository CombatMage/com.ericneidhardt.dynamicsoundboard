package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import rx.Observable

/**
 * Created by eric.neidhardt@gmail.com on 06.12.2016.
 */
interface ISoundLayoutManager {

	val soundLayouts: List<SoundLayout>

	fun addSoundLayout(soundLayout: SoundLayout): Observable<SoundLayout>

	fun updateSoundLayout(update: () -> SoundLayout): Observable<SoundLayout>

	fun setSoundLayoutSelected(soundLayout: SoundLayout)

	fun removeSoundLayouts(toRemove: List<SoundLayout>): Observable<List<SoundLayout>>

	fun getSuggestedName(): String
}

val List<SoundLayout>.selectedLayout: SoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<SoundLayout>.activeLayout: SoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")

fun List<SoundLayout>.findById(databaseId: String): SoundLayout?
	= this.firstOrNull { it.databaseId == databaseId }