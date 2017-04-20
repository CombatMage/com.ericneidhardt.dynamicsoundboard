package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.view_sound_control_item.view.*
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.containsPlayerWithId
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.views.sound_control.PlayButton
import org.neidhardt.dynamicsoundboard.views.sound_control.ToggleLoopButton
import org.neidhardt.dynamicsoundboard.views.sound_control.TogglePlaylistButton

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundViewHolder(
		itemView: View,
		private val playlistManager: PlaylistManager
) :
		RecyclerView.ViewHolder(itemView)
{
	val reorder: View = itemView.ib_view_sound_control_item_reorder

	val name: TextView = itemView.textview_soundcontrolitem_soundname

	val playButton: PlayButton = itemView.ib_view_sound_control_item_play
	val stopButton: ImageButton = itemView.ib_view_sound_control_item_stop

	val isLoopEnabledButton: ToggleLoopButton = itemView.ib_view_sound_control_item_loop
	val inPlaylistButton: TogglePlaylistButton = itemView.ib_view_sound_control_item_add_to_playlist
	
	val timePosition: SeekBar= itemView.sb_view_sound_control_item_progress
	val settingsButton: ImageButton = itemView.ib_view_sound_control_item_settings

	var player: MediaPlayerController? = null

	fun bindData(player: MediaPlayerController) {
		this.player = player

		player.setOnProgressChangedEventListener { progress, _ ->
			this.timePosition.max = player.trackDuration
			this.timePosition.progress = progress
		}
		this.updateViewToPlayerState()
	}

	fun updateViewToPlayerState() {
		this.player?.let { player ->
			val playerData = player.mediaPlayerData

			this.name.text = playerData.label

			if (player.isFadingOut)
				this.playButton.state = PlayButton.State.FADE
			else if (player.isPlayingSound) // if already playing, we enable pause
				this.playButton.state = PlayButton.State.PAUSE
			else
				this.playButton.state = PlayButton.State.PLAY

			this.stopButton.isEnabled = player.isPlayingSound || player.progress > 0

			this.isLoopEnabledButton.state = if (playerData.isLoop)
						ToggleLoopButton.State.LOOP_ENABLE
					else
						ToggleLoopButton.State.LOOP_DISABLE

			this.inPlaylistButton.state =
					if (this.playlistManager.playlist.containsPlayerWithId(playerData.playerId))
						TogglePlaylistButton.State.IN_PLAYLIST
					else
						TogglePlaylistButton.State.NOT_IN_PLAYLIST

			this.timePosition.isEnabled = player.isPlayingSound // only active player is prepared and can seekTo
			this.timePosition.max = player.trackDuration
			this.timePosition.progress = player.progress
		}
	}

}
