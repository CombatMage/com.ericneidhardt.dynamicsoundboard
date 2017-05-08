package org.neidhardt.dynamicsoundboard.soundcontrol.views

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment
import org.neidhardt.dynamicsoundboard.views.sound_control.PlayButton
import java.lang.ref.WeakReference

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
class SoundPresenter (
		private val adapter: SoundAdapter,
		private val playlistManager: PlaylistManager,
		fragment: SoundSheetFragment
) : MediaPlayerEventListener {

	private val fragmentReference: WeakReference<SoundSheetFragment> = WeakReference(fragment)

	private val eventBus = EventBus.getDefault()

	private val fragment: SoundSheetFragment? get() = this.fragmentReference.get()

	fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)
		this.adapter.notifyDataSetChanged()
	}

	fun onDetachedFromWindow() {
		this.eventBus.unregister(this)
	}

	fun userTogglesPlaybackState(player: MediaPlayerController, playbackButton: PlayButton) {
		if (player.isFadingOut) {
			playbackButton.state = PlayButton.State.PLAY
			player.stopSound()
		}
		else if (player.isPlayingSound) {
			playbackButton.state = PlayButton.State.FADE
			player.fadeOutSound()
		}
		else {
			playbackButton.state = PlayButton.State.PAUSE
			player.playSound()
		}
	}

	fun userStopsPlayback(player: MediaPlayerController) {
		player.stopSound()
		this.adapter.notifyItemChanged(player)
	}

	fun userTogglesPlaylistState(player: MediaPlayerController, addToPlaylist: Boolean) {
		player.mediaPlayerData.let { this.playlistManager.togglePlaylistSound(it, addToPlaylist) }
	}

	fun userRequestPlayerSettings(player: MediaPlayerController) {
		if (player.isPlayingSound)
			player.pauseSound()
		SoundSettingsDialog.showInstance(this.fragment?.fragmentManager, player.mediaPlayerData)
	}

	fun userEnablesLooping(player: MediaPlayerController, enableLoop: Boolean) {
		player.isLoopingEnabled = enableLoop
	}

	fun userSeeksToPlayerPosition(player: MediaPlayerController, position: Int) {
		player.progress = position
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		val playerId = event.playerId
		val players = this.adapter.values
		val count = players.size
		for (i in 0..count - 1) {
			val player = players[i]
			if (event.isAlive && player.mediaPlayerData.playerId == playerId && !player.isDeletionPending)
				this.adapter.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}
