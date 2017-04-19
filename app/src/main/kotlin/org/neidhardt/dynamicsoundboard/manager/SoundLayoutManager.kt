package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.AppData
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout

/**
* @author Eric.Neidhardt@GMail.com on 19.12.2016.
*/
val DB_DEFAULT = "Soundboard_db"

class SoundLayoutManager(
		private val context: Context,
		private val soundSheetManager: SoundSheetManager,
		private val playlistManager: PlaylistManager,
		private val soundManager: SoundManager) {

	internal var onSoundLayoutsChangedListener = ArrayList<(List<NewSoundLayout>) -> Unit>()
	internal var onPlayingSoundsChangedListener = ArrayList<(List<MediaPlayerController>) -> Unit>()
	internal var onLoadingCompletedListener = ArrayList<(List<NewSoundLayout>) -> Unit>()

	internal var mSoundLayouts: MutableList<NewSoundLayout>? = null
	val soundLayouts: List<NewSoundLayout> get() = this.mSoundLayouts as List<NewSoundLayout>

	internal val mCurrentlyPlayingSounds = ArrayList<MediaPlayerController>()
	val currentlyPlayingSounds: List<MediaPlayerController> get() = this.mCurrentlyPlayingSounds

	@Synchronized
	fun initIfRequired(appData: Observable<AppData?>) {
		if (mSoundLayouts == null) {
			this.mSoundLayouts = ArrayList()
			appData.subscribe { appData ->
				this.mSoundLayouts = ArrayList()
				appData?.soundLayouts?.let { this.mSoundLayouts?.addAll(it) }
				if (this.mSoundLayouts?.isEmpty() == true)
					this.mSoundLayouts?.add(this.getDefaultSoundLayout())

				this.setSoundSheetsForActiveLayout()
				this.onLoadingCompletedListener.forEach { it.invoke(this.soundLayouts) }
				this.invokeListeners()
			}
		}
	}

	@Synchronized
	fun init(appData: Observable<AppData?>) {
		appData.subscribe { appData ->
			this.mSoundLayouts = ArrayList()
			this.mSoundLayouts = ArrayList()
			appData?.soundLayouts?.let { this.mSoundLayouts?.addAll(it) }
			if (this.mSoundLayouts?.isEmpty() == true)
				this.mSoundLayouts?.add(this.getDefaultSoundLayout())

			this.setSoundSheetsForActiveLayout()
			this.onLoadingCompletedListener.forEach { it.invoke(this.soundLayouts) }
			this.invokeListeners()
		}
	}

	fun remove(soundLayouts: List<NewSoundLayout>) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")

		this.mSoundLayouts?.let { mSoundLayouts ->
			mSoundLayouts.removeAll(soundLayouts)
			if (mSoundLayouts.isEmpty()) {
				mSoundLayouts.add(this.getDefaultSoundLayout())
				this.setSoundSheetsForActiveLayout()
			}
			else if (mSoundLayouts.selectedLayout == null) {
				mSoundLayouts[0].isSelected = true
				this.setSoundSheetsForActiveLayout()
			}

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
		this.setSoundSheetsForActiveLayout()
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

	val suggestedName: String get() = this.context.resources.getString(R.string.suggested_sound_layout_name) +
			(this.mSoundLayouts?.size ?: "")

	private fun getDefaultSoundLayout(): NewSoundLayout =
			NewSoundLayout().apply {
				this.databaseId = DB_DEFAULT
				this.label = context.resources.getString(R.string.suggested_sound_layout_name)
				this.isSelected = true
			}

	private fun setSoundSheetsForActiveLayout() {
		val activeLayout = this.soundLayouts.activeLayout
		if (activeLayout.soundSheets == null)
			activeLayout.soundSheets = ArrayList()
		this.soundSheetManager.set(activeLayout.soundSheets!!)
		this.soundManager.set(activeLayout.soundSheets!!)

		if (activeLayout.playList == null)
			activeLayout.playList = ArrayList()
		this.playlistManager.set(activeLayout.playList!!)

		this.mCurrentlyPlayingSounds.clear()
	}

	fun removeSoundFromCurrentlyPlayingSounds(player: MediaPlayerController) {
		if (this.mCurrentlyPlayingSounds.contains(player)) {
			this.mCurrentlyPlayingSounds.remove(player)
			this.onPlayingSoundsChangedListener.forEach { it.invoke(this.currentlyPlayingSounds) }
		}
	}

	fun addSoundToCurrentlyPlayingSounds(player: MediaPlayerController) {
		this.mCurrentlyPlayingSounds.add(player)
		this.onPlayingSoundsChangedListener.forEach { it.invoke(this.currentlyPlayingSounds) }
	}

	companion object {
		fun getNewDatabaseIdForLabel(label: String): String {
			return Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		}
	}
}

object RxNewSoundLayoutManager {

	fun soundLayoutsChanges(manager: SoundLayoutManager): Observable<List<NewSoundLayout>> {
		return Observable.create { subscriber ->
			val listener: (List<NewSoundLayout>) -> Unit = {
				subscriber.onNext(it)
			}
			//subscriber.add(Subscriptions.create {
			//	manager.onSoundLayoutsChangedListener.remove(listener)
			//})
			manager.mSoundLayouts?.let { subscriber.onNext(it) }
			manager.onSoundLayoutsChangedListener.add(listener)
		}
	}

	fun changesPlayingSounds(manager: SoundLayoutManager): Observable<List<MediaPlayerController>> {
		return Observable.create { subscriber ->
			val listener: (List<MediaPlayerController>) -> Unit = {
				subscriber.onNext(it)
			}
			//subscriber.add(Subscriptions.create {
			//	manager.onPlayingSoundsChangedListener.remove(listener)
			//})
			manager.mCurrentlyPlayingSounds.let{ subscriber.onNext(it) }
			manager.onPlayingSoundsChangedListener.add(listener)
		}
	}

	fun completesLoading(manager: SoundLayoutManager): Observable<List<NewSoundLayout>> {
		return Observable.create { subscriber ->
			val listener: (List<NewSoundLayout>) -> Unit = {
				subscriber.onNext(it)
				subscriber.onComplete()
			}
			//subscriber.add(Subscriptions.create {
			//	manager.onLoadingCompletedListener.remove(listener)
			//})
			manager.mSoundLayouts?.let {
				subscriber.onNext(it)
				subscriber.onComplete()
			}
			manager.onLoadingCompletedListener.add(listener)
		}
	}
}

val List<NewSoundLayout>.selectedLayout: NewSoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<NewSoundLayout>.activeLayout: NewSoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")