package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.persistance.AppDataStorage
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutManager
import rx.Observable
import rx.lang.kotlin.add
import java.util.*

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class NewSoundLayoutManager(
		private val context: Context,
		private val storage: AppDataStorage,
		private val newSoundSheetManager: NewSoundSheetManager,
		private val newSoundManager: NewSoundManager) {

	internal var onSoundLayoutsChangedListener = ArrayList<((List<NewSoundLayout>) -> Unit)>()

	internal var mSoundLayouts: MutableList<NewSoundLayout>? = null

	val soundLayouts: List<NewSoundLayout> get() = this.mSoundLayouts as List<NewSoundLayout>

	@Synchronized
	fun initIfRequired() {
		if (mSoundLayouts == null) {
			this.mSoundLayouts = ArrayList()
			this.storage.get().subscribe { appData ->
				this.mSoundLayouts = ArrayList()
				appData?.soundLayouts?.let { this.mSoundLayouts?.addAll(it) }
				if (this.mSoundLayouts?.isEmpty() == true)
					this.mSoundLayouts?.add(this.getDefaultSoundLayout())

				this.invokeListeners()
			}
		}
	}

	fun remove(soundLayouts: List<NewSoundLayout>) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")

		this.mSoundLayouts?.let { mSoundLayouts ->
			mSoundLayouts.removeAll(soundLayouts)
			if (mSoundLayouts.isEmpty() == true)
				mSoundLayouts.add(this.getDefaultSoundLayout())
			else if (mSoundLayouts.selectedLayout == null)
				mSoundLayouts[0].isSelected = true
			this.invokeListeners()
		}
	}

	fun add(soundLayout: NewSoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")

		this.mSoundLayouts?.add(soundLayout)
		this.invokeListeners()
	}

	fun setSelected(soundLayout: NewSoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")
		if (!this.soundLayouts.contains(soundLayout)) throw IllegalArgumentException("given layout not found in dataset")

		this.soundLayouts.forEach { it.isSelected = it == soundLayout }
		this.invokeListeners()
	}

	fun notifyHasChanged(soundLayout: NewSoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")
		if (!this.soundLayouts.contains(soundLayout)) throw IllegalArgumentException("given layout not found in dataset")
		this.invokeListeners()
	}

	private fun invokeListeners() {
		this.onSoundLayoutsChangedListener.forEach { it.invoke(this.soundLayouts) }
	}

	fun getSuggestedName(): String {
		val count = this.mSoundLayouts?.size ?: 0
		return this.context.resources.getString(R.string.suggested_sound_layout_name) + count
	}

	private fun getDefaultSoundLayout(): NewSoundLayout =
			NewSoundLayout().apply {
				this.databaseId = SoundLayoutManager.DB_DEFAULT
				this.label = context.resources.getString(R.string.suggested_sound_layout_name)
				this.isSelected = true
			}
}

object RxNewSoundLayoutManager {
	fun soundLayoutsChanged(manager: NewSoundLayoutManager): Observable<List<NewSoundLayout>> {
		return Observable.create { subscriber ->
			val listener: (List<NewSoundLayout>) -> Unit = {
				subscriber.onNext(it)
			}
			subscriber.add {
				manager.onSoundLayoutsChangedListener.remove(listener)
			}
			manager.mSoundLayouts?.let { subscriber.onNext(it) }
			manager.onSoundLayoutsChangedListener.add(listener)
		}
	}
}

val List<NewSoundLayout>.selectedLayout: NewSoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<NewSoundLayout>.activeLayout: NewSoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")