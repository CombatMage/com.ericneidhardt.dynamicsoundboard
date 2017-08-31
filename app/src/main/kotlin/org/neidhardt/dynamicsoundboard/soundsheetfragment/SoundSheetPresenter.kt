package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.net.Uri
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.manager.findById
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.mediaplayer.events.*
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.utils.getCopyList

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
class SoundSheetPresenter(
		private val soundSheetView: SoundSheetContract.View,
		private val lifecycleProvider: LifecycleProvider<FragmentEvent>
) : SoundSheetContract.Presenter, MediaPlayerEventListener, MediaPlayerFailedEventListener {

	private val eventBus = EventBus.getDefault()
	private val soundManager = SoundboardApplication.soundManager
	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
	private val playlistManager = SoundboardApplication.playlistManager

	override fun onViewResumed() {
		this.soundSheetView.showSounds()
		// if sounds where removed, the view may becomes unscrollable
		// and therefore the fab can not be reached
		RxSoundManager.changesSoundList(this.soundManager)
				.bindToLifecycle(this.lifecycleProvider)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { sounds ->
					if (sounds.isEmpty())
						this.soundSheetView.showAddButton()
				}
		RxSoundManager.changesSoundList(this.soundManager)
				.bindToLifecycle(this.lifecycleProvider)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.soundSheetView.showSounds()
				}
		RxNewPlaylistManager.playlistChanges(this.playlistManager)
				.bindToLifecycle(this.lifecycleProvider)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.soundSheetView.showSounds()
				}
		this.eventBus.registerIfRequired(this)
	}

	override fun onViewPaused() {
		this.eventBus.unregister(this)
	}

	override fun onUserClicksFab() {
		val currentlyPlayingSounds = this.soundLayoutManager.currentlyPlayingSounds
		if (currentlyPlayingSounds.isNotEmpty()) {
			val copyCurrentlyPlayingSounds = currentlyPlayingSounds.getCopyList()
			for (sound in copyCurrentlyPlayingSounds)
				sound.pauseSound()
		}
		else {
			this.soundSheetView.openDialogForNewSound()
		}
	}

	override fun onUserClicksPlay(player: MediaPlayerController) {
		if (player.isFadingOut)
			player.stopSound()
		else if (player.isPlayingSound)
			player.fadeOutSound()
		else
			player.playSound()
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
		this.soundSheetView.showSnackbarForRestoreSound()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		val player = event.player
		if (event.isAlive && !player.isDeletionPending)
			this.soundSheetView.updateSound(player)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerFailedEvent) {
		this.soundSheetView.showSnackbarForPlayerError(event.player)
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}