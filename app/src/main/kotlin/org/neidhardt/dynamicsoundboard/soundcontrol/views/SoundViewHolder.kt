package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.layout_sound_control.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressTimer
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressViewHolder
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
private val VIEWPAGER_INDEX_SOUND_CONTROLS = 1

class SoundViewHolder
(
		itemTouchHelper: ItemTouchHelper,
		itemView: View,
		private val eventBus: EventBus,
		private val soundsDataStorage: SoundsDataStorage,
		private val progressTimer: SoundProgressTimer
) :
		RecyclerView.ViewHolder(itemView),
		SoundProgressViewHolder,
		CustomEditText.OnTextEditedListener,
		SeekBar.OnSeekBarChangeListener
{
    private val TAG = javaClass.name

	private val reorder = itemView.ib_layout_sound_control_reorder

	private val name = itemView.et_layout_sound_control_name
	private val play = itemView.ib_layout_sound_control_play
	private val loop = itemView.ib_layout_sound_control_loop
	private val inPlaylist = itemView.ib_layout_sound_control_add_to_playlist
	private val stop = itemView.ib_layout_sound_control_stop
	private val settings = itemView.ib_layout_sound_control_settings
	private val timePosition = itemView.sb_layout_sound_control_progress

	private var player: MediaPlayerController by Delegates.notNull()

	init
	{
		this.reorder.setOnTouchListener { view, motionEvent ->
			itemTouchHelper.startDrag(this)
			true
		}

		this.play.setOnClickListener { view ->
			this.name.clearFocus()
			if (!view.isSelected)
			{
				this.progressTimer.startProgressUpdateTimer()
				player.playSound()
			}
			else
			{
				player.fadeOutSound()
			}
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

	fun bindData(data: MediaPlayerController)
	{
		this.player = data

		this.updateViewToPlayerState()
	}

	private fun updateViewToPlayerState()
	{
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

	override fun onProgressUpdate()
	{
		this.player.let { player ->
			if (!player.isDeletionPending)
				this.timePosition.progress = player.progress
		}
	}

	override fun onTextEdited(text: String)
	{
		Logger.d(TAG, "onTextEdited: $text")

		this.name.clearFocus()
		this.player.mediaPlayerData.let { playerData ->
			val currentLabel = playerData.label
			if (!currentLabel.equals(text))
			{
				playerData.label = text
				playerData.updateItemInDatabaseAsync()

				this.eventBus.post(OpenSoundRenameEvent(playerData))
			}
		}
	}

	override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
	{
		if (fromUser)
			this.player.progress = progress
	}

	override fun onStartTrackingTouch(seekBar: SeekBar?) {
		Logger.d(TAG, "onStartTrackingTouch")
		this.progressTimer.stopProgressUpdateTimer()
	}

	override fun onStopTrackingTouch(seekBar: SeekBar)
	{
		Logger.d(TAG, "onStopTrackingTouch")
		this.progressTimer.startProgressUpdateTimer()
	}

}
