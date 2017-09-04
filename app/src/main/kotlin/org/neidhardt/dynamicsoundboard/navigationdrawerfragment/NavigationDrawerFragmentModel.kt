package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.neidhardt.dynamicsoundboard.manager.*
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 04.09.2017.
 */
class NavigationDrawerFragmentModel(
		private val soundManager: SoundManager,
		private val soundSheetManager: SoundSheetManager,
		private val playlistManager: PlaylistManager,
		private val soundLayoutManager: SoundLayoutManager
) :
		NavigationDrawerFragmentContract.Model
{

	override val soundSheets: Observable<List<SoundSheet>>
		get() {
			return RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val playList: Observable<List<MediaPlayerController>>
		get() {
			return RxNewPlaylistManager.playlistChanges(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val soundLayouts: Observable<List<SoundLayout>>
		get() {
			return RxNewSoundLayoutManager.soundLayoutsChanges(this.soundLayoutManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val mediaPlayerCompletedEvents: Observable<MediaPlayerCompletedEvent>
		get() {
			return RxNewPlaylistManager.playlistPlayerCompletes(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val mediaPlayerStateChangedEvents: Observable<MediaPlayerStateChangedEvent>
		get() {
			return RxNewPlaylistManager.playlistPlayerStateChanges(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override fun setSoundSheetSelected(soundSheet: SoundSheet) {
		this.soundSheetManager.setSelected(soundSheet)
	}

	override fun setSoundLayoutSelected(soundLayout: SoundLayout) {
		this.soundLayoutManager.setSelected(soundLayout)
	}

	override fun deleteSoundSheets(soundSheets: List<SoundSheet>) {
		for (soundSheet in soundSheets) {
			// remove all sounds of this soundSheet to free resources
			this.soundManager.sounds[soundSheet]?.let {
				this.soundManager.remove(soundSheet, it)
			}
		}
		this.soundSheetManager.remove(soundSheets)
	}

	override fun deletePlayListPlayer(player: List<MediaPlayerController>) {
		this.playlistManager.remove(player)
	}

	override fun deleteSoundLayouts(soundLayouts: List<SoundLayout>) {
		this.soundLayoutManager.remove(soundLayouts)
	}
}