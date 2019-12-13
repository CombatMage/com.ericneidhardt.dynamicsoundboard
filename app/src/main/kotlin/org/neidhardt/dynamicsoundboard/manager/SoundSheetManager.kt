package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
* @author Eric.Neidhardt@GMail.com on 19.12.2016.
*/
open class SoundSheetManager {

	companion object {
		fun getNewFragmentTagForLabel(label: String): String = (label + SoundboardApplication.randomNumber).hashCode().toString()
	}

	internal var onSoundSheetsChangedListener = ArrayList<((List<SoundSheet>) -> Unit)>()

	internal var mSoundSheets: MutableList<SoundSheet>? = null

	val soundSheets: List<SoundSheet> get() = this.mSoundSheets as List<SoundSheet>

	fun set(soundSheets: MutableList<SoundSheet>) {
		this.mSoundSheets = soundSheets
		this.invokeListeners()
	}

	fun remove(soundSheets: List<SoundSheet>) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		this.mSoundSheets?.removeAll(soundSheets)
		this.invokeListeners()
	}

	fun add(soundSheet: SoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		this.mSoundSheets?.add(soundSheet)
		this.invokeListeners()
	}

	fun setSelected(soundSheet: SoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		if (!this.soundSheets.contains(soundSheet)) throw IllegalArgumentException("given layout not found in dataset")

		this.soundSheets.forEach { it.isSelected = it == soundSheet }
		this.invokeListeners()
	}

	fun notifyHasChanged(soundSheet: SoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		if (!this.soundSheets.contains(soundSheet)) throw IllegalArgumentException("given layout not found in dataset")
		this.invokeListeners()
	}

	fun getSuggestedName(context: Context): String {
		return context.resources.getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size
	}

	private fun invokeListeners() {
		this.onSoundSheetsChangedListener.forEach { it.invoke(this.soundSheets) }
	}
}

object RxNewSoundSheetManager {
	fun soundSheetsChanged(manager: SoundSheetManager): Observable<List<SoundSheet>> {
		return Observable.create { emitter ->
			val listener: (List<SoundSheet>) -> Unit = {
				emitter.onNext(it)
			}

			emitter.setCancellable {
				manager.onSoundSheetsChangedListener.remove(listener)
			}

			manager.mSoundSheets?.let { emitter.onNext(it) }
			manager.onSoundSheetsChangedListener.add(listener)
		}
	}
}

val List<SoundSheet>.selectedSoundSheet: SoundSheet?
	get() = this.firstOrNull { it.isSelected }

fun List<SoundSheet>.findByFragmentTag(fragmentTag: String): SoundSheet? {
	return this.firstOrNull { it.fragmentTag == fragmentTag }
}