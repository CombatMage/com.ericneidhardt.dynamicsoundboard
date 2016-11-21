package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.view_sound_control_item.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.RxMediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.ui_utils.views.CustomEditText
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
class SoundViewHolder
(
		itemTouchHelper: ItemTouchHelper,
		itemView: View,
		private val eventBus: EventBus,
		private val soundsDataStorage: SoundsDataStorage
) :
		RecyclerView.ViewHolder(itemView),
		MediaPlayerController.OnProgressChangedEventListener,
		CustomEditText.OnTextEditedListener,
		SeekBar.OnSeekBarChangeListener
{
    private val TAG = javaClass.name

	private val reorder = itemView.ib_view_sound_control_item_reorder

	private val name = itemView.et_view_sound_control_item_name
	private val play = itemView.ib_view_sound_control_item_play
	private val loop = itemView.ib_view_sound_control_item_loop
	private val stop = itemView.ib_view_sound_control_item_stop

	private val inPlaylist = itemView.ib_view_sound_control_item_add_to_playlist
	private val timePosition = itemView.sb_view_sound_control_item_progress
	private val settings = itemView.ib_view_sound_control_item_settings

	private var player: MediaPlayerController by Delegates.notNull()

	init {
		this.reorder.setOnTouchListener { view, motionEvent ->
			itemTouchHelper.startDrag(this)
			true
		}

		this.play.setOnClickListener { view ->
			this.name.clearFocus()
			if (!view.isSelected)
				player.playSound()
			else
				player.fadeOutSound()
		}

		this.loop.setOnClickListener{ view ->
			val isSelected = view.isSelected
			view.isSelected = !isSelected
			this.player.isLoopingEnabled = !isSelected
		}

		this.inPlaylist.setOnClickListener { view ->
			val isSelected = view.isSelected
			view.isSelected = !isSelected
			this.player.isInPlaylist = !isSelected
			this.player.mediaPlayerData.updateItemInDatabaseAsync()
			this.soundsDataStorage.toggleSoundInPlaylist(player.mediaPlayerData.playerId, !isSelected)
		}

		this.stop.setOnClickListener {
			this.player.stopSound()
			this.updateViewToPlayerState()
		}

		this.settings.setOnClickListener {
			this.player.pauseSound()
			this.eventBus.post(OpenSoundSettingsEvent(player.mediaPlayerData))
		}

		this.name.onTextEditedListener = this
		this.timePosition.setOnSeekBarChangeListener(this)
	}

	fun bindData(player: MediaPlayerController)
	{
		this.player = player
		//data.onProgressChangedEventListener = this

		RxMediaPlayerController.plays(player).subscribe { progress ->
			this.timePosition.progress = progress
		}

		this.updateViewToPlayerState()
	}

	private fun updateViewToPlayerState() {
		val player = this.player
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

	override fun onProgressChanged(progress: Int) {
		if (!this.player.isDeletionPending)
			this.timePosition.progress = progress
	}

	override fun onTextEdited(text: String) {
		Logger.d(TAG, "onTextEdited: $text")

		this.name.clearFocus()
		this.player.mediaPlayerData.let { playerData ->
			val currentLabel = playerData.label
			if (currentLabel != text)
			{
				playerData.label = text
				playerData.updateItemInDatabaseAsync()

				this.eventBus.post(OpenSoundRenameEvent(playerData))
			}
		}
	}

	override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
		if (fromUser)
			this.player.progress = progress
	}

	override fun onStartTrackingTouch(seekBar: SeekBar?) {}

	override fun onStopTrackingTouch(seekBar: SeekBar) {}

}
