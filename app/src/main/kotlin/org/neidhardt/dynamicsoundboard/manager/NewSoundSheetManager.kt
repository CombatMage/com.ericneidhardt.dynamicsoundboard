package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import rx.Observable
import rx.lang.kotlin.add
import java.util.*

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class NewSoundSheetManager(private val context: Context) {

	companion object {
		fun getNewFragmentTagForLabel(label: String): String {
			return Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		}
	}

	internal var onSoundSheetsChangedListener = ArrayList<((List<NewSoundSheet>) -> Unit)>()

	internal var mSoundSheets: MutableList<NewSoundSheet>? = null

	val soundSheets: List<NewSoundSheet> get() = this.mSoundSheets as List<NewSoundSheet>

	fun set(soundSheets: MutableList<NewSoundSheet>) {
		this.mSoundSheets = soundSheets
		this.invokeListeners()
	}

	fun remove(soundSheets: List<NewSoundSheet>) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		this.mSoundSheets?.removeAll(soundSheets)
		this.invokeListeners()
	}

	fun add(soundSheet: NewSoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		this.mSoundSheets?.add(soundSheet)
		this.invokeListeners()
	}

	fun setSelected(soundSheet: NewSoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		if (!this.soundSheets.contains(soundSheet)) throw IllegalArgumentException("given layout not found in dataset")

		this.soundSheets.forEach { it.isSelected = it == soundSheet }
		this.invokeListeners()
	}

	fun notifyHasChanged(soundSheet: NewSoundSheet) {
		if (this.mSoundSheets == null) throw IllegalStateException("sound sheet init not done")
		if (!this.soundSheets.contains(soundSheet)) throw IllegalArgumentException("given layout not found in dataset")
		this.invokeListeners()
	}

	val suggestedName: String get() = this.context.resources.getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size

	private fun invokeListeners() {
		this.onSoundSheetsChangedListener.forEach { it.invoke(this.soundSheets) }
	}
}

object RxNewSoundSheetManager {
	fun soundSheetsChanged(manager: NewSoundSheetManager): Observable<List<NewSoundSheet>> {
		return Observable.create { subscriber ->
			val listener: (List<NewSoundSheet>) -> Unit = {
				subscriber.onNext(it)
			}
			subscriber.add {
				manager.onSoundSheetsChangedListener.remove(listener)
			}
			manager.mSoundSheets?.let { subscriber.onNext(it) }
			manager.onSoundSheetsChangedListener.add(listener)
		}
	}
}

val List<NewSoundSheet>.selectedSoundSheet: NewSoundSheet?
	get() = this.firstOrNull { it.isSelected }

fun List<NewSoundSheet>.findByFragmentTag(fragmentTag: String): NewSoundSheet? {
	return this.firstOrNull { it.fragmentTag == fragmentTag }
}