package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.net.Uri
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.findById
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.app_utils.getCopyList

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
class SoundSheetFragmentPresenter(
		private val view: SoundSheetFragmentContract.View,
		private val model: SoundSheetFragmentContract.Model
) : SoundSheetFragmentContract.Presenter {

	private val soundManager = SoundboardApplication.soundManager
	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
	private val playlistManager = SoundboardApplication.playlistManager

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
		val currentlyPlayingSounds = this.soundLayoutManager.currentlyPlayingSounds
		if (currentlyPlayingSounds.isNotEmpty()) {
			val copyCurrentlyPlayingSounds = currentlyPlayingSounds.getCopyList()
			for (sound in copyCurrentlyPlayingSounds)
				sound.pauseSound()
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
		val playerData = player.mediaPlayerData
		val isInPlaylist = this.playlistManager.playlist.findById(playerData.playerId) != null
		this.playlistManager.togglePlaylistSound(playerData, !isInPlaylist)
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
		this.soundManager.add(soundSheet, playerData)
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