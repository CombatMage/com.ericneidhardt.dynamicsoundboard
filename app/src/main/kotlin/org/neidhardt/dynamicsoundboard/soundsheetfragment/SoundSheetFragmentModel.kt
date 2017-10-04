package org.neidhardt.dynamicsoundboard.soundsheetfragment

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.neidhardt.dynamicsoundboard.manager.*
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 26.09.2017.
 */
class SoundSheetFragmentModel(
		private val soundSheet: SoundSheet,
		private val soundManager: SoundManager,
		private val playlistManager: PlaylistManager
) : SoundSheetFragmentContract.Model {

	override val playList: Observable<List<MediaPlayerController>>
		get() {
			return RxNewPlaylistManager.playlistChanges(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val sounds: Observable<List<MediaPlayerController>>
		get() {
			return RxSoundManager.changesSoundList(this.soundManager)
					.map { soundMap -> soundMap[this.soundSheet] ?: emptyList<MediaPlayerController>()}
					.observeOn(AndroidSchedulers.mainThread())
		}
}