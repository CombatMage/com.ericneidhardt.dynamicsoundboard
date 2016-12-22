package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import kotlinx.android.synthetic.main.view_sound_control_item.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.manager.NewSoundManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.viewextensions.setOnUserChangesListener

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundViewHolder
(
		itemTouchHelper: ItemTouchHelper,
		itemView: View,
		private val eventBus: EventBus,
		private val manager: NewSoundManager
) :
		RecyclerView.ViewHolder(itemView)
{
	private val reorder = itemView.ib_view_sound_control_item_reorder

	private val name = itemView.et_view_sound_control_item_name
	private val play = itemView.ib_view_sound_control_item_play
	private val loop = itemView.ib_view_sound_control_item_loop
	private val stop = itemView.ib_view_sound_control_item_stop

	private val inPlaylist = itemView.ib_view_sound_control_item_add_to_playlist
	private val timePosition = itemView.sb_view_sound_control_item_progress
	private val settings = itemView.ib_view_sound_control_item_settings

	private var player: MediaPlayerController? = null

	init {
		this.reorder.setOnTouchListener { view, motionEvent ->
			itemTouchHelper.startDrag(this)
			true
		}

		this.play.setOnClickListener {
			this.player?.let { player ->
				this.name.clearFocus()
				if (!this.play.isSelected) {
					player.playSound()
				}
				else
					player.fadeOutSound()
			}
		}

		this.stop.setOnClickListener {
			this.player?.stopSound()
			this.updateViewToPlayerState()
		}

		this.loop.setOnClickListener {
			val toggleState = !this.loop.isSelected
			this.loop.isSelected = toggleState
			this.player?.isLoopingEnabled = toggleState
		}

		this.inPlaylist.setOnClickListener {
			this.player?.let { player ->
				val toggleState = !this.inPlaylist.isSelected
				this.inPlaylist.isSelected = toggleState
				this.player?.isInPlaylist = toggleState
				//this.manager.toggleSoundInPlaylist(player.mediaPlayerData.playerId, toggleState)
			}
		}

		this.settings.setOnClickListener {
			this.player?.let { player ->
				player.pauseSound()
				this.eventBus.post(OpenSoundSettingsEvent(player.mediaPlayerData))
			}
		}

		this.name.setOnTextEditedListener { newName ->
			this.name.clearFocus()
			this.player?.mediaPlayerData?.let { playerData ->
				val currentLabel = playerData.label
				if (currentLabel != newName) {
					playerData.label = newName
					//this.eventBus.post(OpenSoundRenameEvent(playerData))
				}
			}
		}

		this.timePosition.setOnUserChangesListener { progress ->
			this.player?.progress = progress
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

	private fun updateViewToPlayerState() {
		this.player?.let { player ->
			val playerData = player.mediaPlayerData

			if (!this.name.hasFocus())
				this.name.text = playerData.label

			val isPlaying = player.isPlayingSound
			this.play.isSelected = isPlaying
			this.loop.isSelected = playerData.isLoop
			this.inPlaylist.isSelected = playerData.isInPlaylist
			this.timePosition.max = player.trackDuration
			this.timePosition.progress = player.progress
		}
	}

}
