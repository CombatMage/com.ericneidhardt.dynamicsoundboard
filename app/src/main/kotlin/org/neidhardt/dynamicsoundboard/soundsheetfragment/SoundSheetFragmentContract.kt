package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.net.Uri
import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
interface SoundSheetFragmentContract {
	interface View {
		var displayedSounds: List<MediaPlayerController>
		var currentPlaylist: List<MediaPlayerController>

		fun openDialogForNewSound()
		fun updateSound(player: MediaPlayerController)
		fun showAddButton()
		fun showSnackbarForPlayerError(player: MediaPlayerController)
		fun showSnackbarForRestoreSound()
	}
	interface Presenter {
		fun viewCreated()
		fun onUserClicksFab()
		fun onUserClicksPlay(player: MediaPlayerController)
		fun onUserClicksStop(player: MediaPlayerController)
		fun onUserTogglePlaylist(player: MediaPlayerController)
		fun onUserTogglePlayerLooping(player: MediaPlayerController)
		fun onUserSeeksToPlaybackPosition(player: MediaPlayerController, position: Int)
		fun onUserAddsNewPlayer(soundUri: Uri, label: String, soundSheet: SoundSheet)
		fun onUserDeletesSound()
		fun onMediaPlayerStateChanges(player: MediaPlayerController, isPlayerAlive: Boolean)
		fun onMediaPlayerFailed(player: MediaPlayerController)
	}
	interface Model {
		enum class TogglePlayListAction {
			ADD_TO_PLAYLIST,
			REMOVE_FROM_PLAYLIST
		}

		val sounds: Observable<List<MediaPlayerController>>
		val playList: Observable<List<MediaPlayerController>>
		fun togglePlayerInPlayList(player: MediaPlayerController, action: TogglePlayListAction)
		fun isSoundInPlayList(player: MediaPlayerController): Boolean
		fun addMediaPlayerToSoundSheet(soundSheet: SoundSheet, playerData: MediaPlayerData)
		fun getCurrentlyPlayingSounds(): List<MediaPlayerController>
	}
}