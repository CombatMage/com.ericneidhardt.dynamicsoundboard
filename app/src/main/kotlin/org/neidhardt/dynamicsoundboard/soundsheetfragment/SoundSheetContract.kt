package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.net.Uri
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
interface SoundSheetContract {
	interface View {
		fun openDialogForNewSound()
		fun updateSound(player: MediaPlayerController)
		fun showSounds()
		fun showAddButton()
		fun showSnackbarForPlayerError(player: MediaPlayerController)
		fun showSnackbarForRestoreSound()
	}
	interface Presenter {
		fun onViewResumed()
		fun onViewPaused()
		fun onUserClicksFab()
		fun onUserClicksPlay(player: MediaPlayerController)
		fun onUserClicksStop(player: MediaPlayerController)
		fun onUserTogglePlaylist(player: MediaPlayerController)
		fun onUserTogglePlayerLooping(player: MediaPlayerController)
		fun onUserSeeksToPlaybackPosition(player: MediaPlayerController, position: Int)
		fun onUserAddsNewPlayer(soundUri: Uri, label: String, soundSheet: SoundSheet)
		fun onUserDeletesSound()
	}
}