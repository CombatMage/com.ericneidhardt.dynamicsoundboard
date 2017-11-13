package org.neidhardt.dynamicsoundboard.soundsheetfragment

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.*
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 26.09.2017.
 */
class SoundSheetFragmentModel(
		private val soundLayoutManager: SoundLayoutManager,
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

	override fun getCurrentlyPlayingSounds(): List<MediaPlayerController> =
			this.soundLayoutManager.currentlyPlayingSounds

	override fun addMediaPlayerToSoundSheet(soundSheet: SoundSheet, playerData: MediaPlayerData) {
		this.soundManager.add(soundSheet, playerData)
	}

	override fun isSoundInPlayList(player: MediaPlayerController): Boolean {
		val playerData = player.mediaPlayerData
		return this.playlistManager.playlist.findById(playerData.playerId) != null
	}

	override fun togglePlayerInPlayList(player: MediaPlayerController, action: SoundSheetFragmentContract.Model.TogglePlayListAction) {
		val playerData = player.mediaPlayerData
		this.playlistManager.togglePlaylistSound(
				playerData,
				action == SoundSheetFragmentContract.Model.TogglePlayListAction.ADD_TO_PLAYLIST)
	}
}