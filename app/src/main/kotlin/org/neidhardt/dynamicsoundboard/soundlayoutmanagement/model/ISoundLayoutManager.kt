package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import rx.Observable

/**
 * Created by eric.neidhardt@gmail.com on 06.12.2016.
 */
interface ISoundLayoutManager {

	val soundLayouts: List<SoundLayout>

	var onSoundLayoutSelectedListener: ((SoundLayout) -> Unit)?

	var onSoundLayoutsChangedListener: ((List<SoundLayout>) -> Unit)?

	var onSoundLayoutIsChangedListener: ((SoundLayout) -> Unit)?

	var onSoundLayoutsLoadedListener: ((List<SoundLayout>) -> Unit)?

	var isInitDone: Boolean

	fun addSoundLayout(soundLayout: SoundLayout): Observable<SoundLayout>

	fun updateSoundLayout(update: () -> SoundLayout): Observable<SoundLayout>

	fun setSoundLayoutSelected(soundLayout: SoundLayout)

	fun removeSoundLayouts(soundLayouts: List<SoundLayout>): Observable<List<SoundLayout>>

	fun getSuggestedName(): String
}

val List<SoundLayout>.selectedLayout: SoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<SoundLayout>.activeLayout: SoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")

fun List<SoundLayout>.findById(databaseId: String): SoundLayout?
	= this.firstOrNull { it.databaseId == databaseId }

object RxSoundLayoutManager {
	fun loadSoundSheets(manager: ISoundLayoutManager) : Observable<List<SoundLayout>> {
		return Observable.create { subscriber ->
			if (manager.isInitDone)
				subscriber.onNext(manager.soundLayouts)
			manager.onSoundLayoutsLoadedListener = { soundSheet ->
				subscriber.onNext(soundSheet)
			}
		}
	}

	fun changesLayoutList(manager: ISoundLayoutManager): Observable<List<SoundLayout>> {
		return Observable.create { subscriber ->
			subscriber.onNext(manager.soundLayouts)
			manager.onSoundLayoutsChangedListener = { layouts ->
				subscriber.onNext(layouts)
			}
		}
	}

	fun changesLayout(manager: ISoundLayoutManager) : Observable<SoundLayout> {
		return Observable.create { subscriber ->
			manager.onSoundLayoutIsChangedListener = { layout ->
				subscriber.onNext(layout)
			}
		}
	}

	fun selectsLayout(manager: ISoundLayoutManager): Observable<SoundLayout> {
		return Observable.create { subscriber ->
			//val selectedSoundLayout = manager.soundLayouts.first { it.isSelected }
			//subscriber.onNext(selectedSoundLayout)
			manager.onSoundLayoutSelectedListener = { layout ->
				subscriber.onNext(layout)
			}
		}
	}
}