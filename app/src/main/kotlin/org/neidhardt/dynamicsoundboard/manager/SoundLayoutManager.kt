package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.AppData
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.simplestorage.Optional

/**
* @author Eric.Neidhardt@GMail.com on 19.12.2016.
*/
val DB_DEFAULT = "Soundboard_db"

class SoundLayoutManager(
		private val context: Context,
		private val soundSheetManager: SoundSheetManager,
		private val playlistManager: PlaylistManager,
		private val soundManager: SoundManager) {

	internal var onSoundLayoutsChangedListener = ArrayList<(List<SoundLayout>) -> Unit>()
	internal var onPlayingSoundsChangedListener = ArrayList<(List<MediaPlayerController>) -> Unit>()
	internal var onLoadingCompletedListener = ArrayList<(List<SoundLayout>) -> Unit>()

	internal var mSoundLayouts: MutableList<SoundLayout>? = null
	val soundLayouts: List<SoundLayout> get() = this.mSoundLayouts as List<SoundLayout>

	internal val mCurrentlyPlayingSounds = ArrayList<MediaPlayerController>()
	val currentlyPlayingSounds: List<MediaPlayerController> get() = this.mCurrentlyPlayingSounds

	@Synchronized
	fun initIfRequired(loadingAppData: Observable<Optional<AppData>>) {
		if (mSoundLayouts == null) {
			this.mSoundLayouts = ArrayList()
			loadingAppData.subscribe { appData ->
				this.mSoundLayouts = ArrayList()
				if (!appData.isEmpty)
					appData.item?.soundLayouts?.let { this.mSoundLayouts?.addAll(it) }

				if (this.mSoundLayouts?.isEmpty() == true)
					this.mSoundLayouts?.add(this.getDefaultSoundLayout())

				this.setSoundSheetsForActiveLayout()
				this.onLoadingCompletedListener.forEach { it.invoke(this.soundLayouts) }
				this.invokeListeners()
			}
		}
	}

	@Synchronized
	fun init(loadingAppData: Observable<AppData?>) {
		loadingAppData.subscribe { appData ->
			this.mSoundLayouts = ArrayList()
			appData?.soundLayouts?.let { this.mSoundLayouts?.addAll(it) }
			if (this.mSoundLayouts?.isEmpty() == true)
				this.mSoundLayouts?.add(this.getDefaultSoundLayout())

			this.setSoundSheetsForActiveLayout()
			this.onLoadingCompletedListener.forEach { it.invoke(this.soundLayouts) }
			this.invokeListeners()
		}
	}

	fun remove(soundLayouts: List<SoundLayout>) {
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

	fun add(soundLayout: SoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")

		this.mSoundLayouts?.add(soundLayout)
		this.invokeListeners()
	}

	fun setSelected(soundLayout: SoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")
		if (!this.soundLayouts.contains(soundLayout)) throw IllegalArgumentException("given layout not found in dataset")

		this.soundLayouts.forEach { it.isSelected = it == soundLayout }
		this.setSoundSheetsForActiveLayout()
		this.invokeListeners()
	}

	fun notifyHasChanged(soundLayout: SoundLayout) {
		if (this.mSoundLayouts == null) throw IllegalStateException("sound layout init not done")
		if (!this.soundLayouts.contains(soundLayout)) throw IllegalArgumentException("given layout not found in dataset")
		this.invokeListeners()
	}

	private fun invokeListeners() {
		this.onSoundLayoutsChangedListener.forEach { it.invoke(this.soundLayouts) }
	}

	val suggestedName: String get() = this.context.resources.getString(R.string.suggested_sound_layout_name) +
			(this.mSoundLayouts?.size ?: "")

	private fun getDefaultSoundLayout(): SoundLayout =
			SoundLayout().apply {
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
		fun getNewDatabaseIdForLabel(label: String): String =
				Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
	}
}

object RxNewSoundLayoutManager {

	fun soundLayoutsChanges(manager: SoundLayoutManager): Observable<List<SoundLayout>> {
		return Observable.create { subscriber ->
			val listener: (List<SoundLayout>) -> Unit = {
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

	fun completesLoading(manager: SoundLayoutManager): Observable<List<SoundLayout>> {
		return Observable.create { subscriber ->
			val listener: (List<SoundLayout>) -> Unit = {
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

val List<SoundLayout>.selectedLayout: SoundLayout?
	get() = this.firstOrNull { it.isSelected }

val List<SoundLayout>.activeLayout: SoundLayout
	get() = selectedLayout ?: throw IllegalStateException("no sound layout is selected")