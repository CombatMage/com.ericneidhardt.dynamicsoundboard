package org.neidhardt.dynamicsoundboard.soundcontrol.views

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.RenameSoundFileDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment
import org.neidhardt.eventbus_utils.registerIfRequired
import java.lang.ref.WeakReference

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
class SoundPresenter (
		private val playlistManager: PlaylistManager,
		fragment: SoundSheetFragment
) : MediaPlayerEventListener {

	private val fragmentReference: WeakReference<SoundSheetFragment>

	init { this.fragmentReference = WeakReference(fragment) }

	private val eventBus = EventBus.getDefault()

	private val fragment: SoundSheetFragment? get() = this.fragmentReference.get()
	private val adapter: SoundAdapter? get() = this.fragment?.soundAdapter

	fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)
		this.adapter?.notifyDataSetChanged()
	}

	fun onDetachedFromWindow() {
		this.eventBus.unregister(this)
	}

	fun userTogglesPlaybackState(player: MediaPlayerController, startPlayback: Boolean) {
		if (startPlayback)
			player.playSound()
		else
			player.pauseSound()
		// no need to update item, this is done on the player's event
	}

	fun userStopsPlayback(player: MediaPlayerController) {
		player.stopSound()
		this.adapter?.notifyItemChanged(player)
	}

	fun userTogglesPlaylistState(player: MediaPlayerController, addToPlaylist: Boolean) {
		player.mediaPlayerData.let { this.playlistManager.togglePlaylistSound(it, addToPlaylist) }
		this.adapter?.notifyItemChanged(player)
	}

	fun userRequestPlayerSettings(player: MediaPlayerController) {
		if (player.isPlayingSound)
			player.pauseSound()
		SoundSettingsDialog.showInstance(this.fragment?.fragmentManager, player.mediaPlayerData)
	}

	fun userEnablesLooping(player: MediaPlayerController, enableLoop: Boolean) {
		player.isLoopingEnabled = enableLoop
		this.adapter?.notifyItemChanged(player)
	}

	fun userChangesPlayerName(player: MediaPlayerController, name: String) {
		val currentLabel = player.mediaPlayerData.label
		if (currentLabel != name) {
			player.mediaPlayerData.label = name
			RenameSoundFileDialog.show(this.fragment?.fragmentManager, player.mediaPlayerData)
		}
	}

	fun userSeeksToPlayerPosition(player: MediaPlayerController, position: Int) {
		player.progress = position
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		val playerId = event.playerId
		val players = this.adapter?.values ?: return
		val count = players.size
		for (i in 0..count - 1) {
			val player = players[i]
			if (event.isAlive && player.mediaPlayerData.playerId == playerId && !player.isDeletionPending)
				this.adapter?.notifyItemChanged(i)
		}
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}
