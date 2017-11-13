package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.net.Uri
import org.neidhardt.app_utils.getCopyList
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
class SoundSheetFragmentPresenter(
		private val view: SoundSheetFragmentContract.View,
		private val model: SoundSheetFragmentContract.Model
) : SoundSheetFragmentContract.Presenter {

	override fun viewCreated() {
		this.model.playList.subscribe { playList ->
			this.view.currentPlaylist = playList
		}

		this.model.sounds.subscribe { sounds ->
			if (sounds.isEmpty()) {
				this.view.showAddButton()
			}
			this.view.displayedSounds = sounds
		}
	}

	override fun onUserClicksFab() {
		val currentlyPlayingSounds = this.model.getCurrentlyPlayingSounds()

		if (currentlyPlayingSounds.isNotEmpty()) {
			val copyCurrentlyPlayingSounds = currentlyPlayingSounds.getCopyList()
			for (sound in copyCurrentlyPlayingSounds) {
				sound.pauseSound()
			}
		}
		else {
			this.view.openDialogForNewSound()
		}
	}

	override fun onUserClicksPlay(player: MediaPlayerController) {
		when {
			player.isFadingOut -> player.stopSound()
			player.isPlayingSound -> player.fadeOutSound()
			else -> player.playSound()
		}
	}

	override fun onUserClicksStop(player: MediaPlayerController) {
		player.stopSound()
	}

	override fun onUserTogglePlaylist(player: MediaPlayerController) {
		val isInPlaylist = this.model.isSoundInPlayList(player)
		val action = if (isInPlaylist){
			SoundSheetFragmentContract.Model.TogglePlayListAction.REMOVE_FROM_PLAYLIST
		} else {
			SoundSheetFragmentContract.Model.TogglePlayListAction.ADD_TO_PLAYLIST
		}
		this.model.togglePlayerInPlayList(
				player,
				action)
	}

	override fun onUserTogglePlayerLooping(player: MediaPlayerController) {
		player.isLoopingEnabled = !player.isLoopingEnabled
	}

	override fun onUserSeeksToPlaybackPosition(player: MediaPlayerController, position: Int) {
		player.progress = position
	}

	override fun onUserAddsNewPlayer(soundUri: Uri, label: String, soundSheet: SoundSheet) {
		val playerData = MediaPlayerFactory.getNewMediaPlayerData(
				soundSheet.fragmentTag, soundUri, label)

		this.model.addMediaPlayerToSoundSheet(soundSheet, playerData)
	}

	override fun onUserDeletesSound() {
		this.view.showSnackbarForRestoreSound()
	}

	override fun onMediaPlayerStateChanges(player: MediaPlayerController, isPlayerAlive: Boolean) {
		if (isPlayerAlive && !player.isDeletionPending)
			this.view.updateSound(player)
	}

	override fun onMediaPlayerFailed(player: MediaPlayerController) {
		this.view.showSnackbarForPlayerError(player)
	}
}