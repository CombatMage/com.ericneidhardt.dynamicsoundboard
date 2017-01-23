package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import kotlinx.android.synthetic.main.view_sound_control_item.view.*
import org.neidhardt.android_utils.views.CustomEditText
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.containsPlayerWithId
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.views.viewextensions.setOnUserChangesListener

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundViewHolder(
		itemTouchHelper: ItemTouchHelper,
		itemView: View,
		private val playlistManager: PlaylistManager
) :
		RecyclerView.ViewHolder(itemView)
{
	private val reorder = itemView.ib_view_sound_control_item_reorder

	val name: CustomEditText = itemView.et_view_sound_control_item_name
	
	val playButton: ImageButton = itemView.ib_view_sound_control_item_play
	val stopButton: ImageButton = itemView.ib_view_sound_control_item_stop

	val isLoopEnabledButton: ImageButton = itemView.ib_view_sound_control_item_loop
	val inPlaylistButton: ImageButton = itemView.ib_view_sound_control_item_add_to_playlist
	
	val timePosition: SeekBar= itemView.sb_view_sound_control_item_progress
	val settingsButton: ImageButton = itemView.ib_view_sound_control_item_settings

	var player: MediaPlayerController? = null

	init {
		this.reorder.setOnTouchListener { view, motionEvent ->
			itemTouchHelper.startDrag(this)
			true
		}
	}

	fun bindData(player: MediaPlayerController) {
		this.player = player

		player.setOnProgressChangedEventListener { progress, trackDuration ->
			this.timePosition.max = player.trackDuration
			this.timePosition.progress = progress
		}
		this.updateViewToPlayerState()
	}

	fun updateViewToPlayerState() {
		this.player?.let { player ->
			val playerData = player.mediaPlayerData

			if (!this.name.hasFocus())
				this.name.text = playerData.label

			val isPlaying = player.isPlayingSound
			this.playButton.isSelected = isPlaying
			this.isLoopEnabledButton.isSelected = playerData.isLoop
			this.inPlaylistButton.isSelected = this.playlistManager.playlist.containsPlayerWithId(playerData.playerId)
			this.timePosition.max = player.trackDuration
			this.timePosition.progress = player.progress
		}
	}

}
