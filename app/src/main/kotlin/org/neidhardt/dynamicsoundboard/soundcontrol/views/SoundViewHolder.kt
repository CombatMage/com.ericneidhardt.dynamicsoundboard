package org.neidhardt.dynamicsoundboard.soundcontrol.views

import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressTimer
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressViewHolder
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.UPDATE_INTERVAL

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
private val VIEWPAGER_INDEX_SOUND_CONTROLS = 1

class SoundViewHolder
(
		itemView: View,
		private val eventBus: EventBus,
		private val soundsDataStorage: SoundsDataStorage,
		private val progressTimer: SoundProgressTimer
) :
		RecyclerView.ViewHolder(itemView),
		SoundProgressViewHolder,
		View.OnClickListener,
		CustomEditText.OnTextEditedListener,
		SeekBar.OnSeekBarChangeListener
{
    private val TAG = javaClass.name

	private val reorder = itemView.findViewById(R.id.b_reorder)

	private val name = itemView.findViewById(R.id.et_name) as CustomEditText
	private val play = itemView.findViewById(R.id.b_play)
	private val loop = itemView.findViewById(R.id.b_loop)
	private val inPlaylist = itemView.findViewById(R.id.b_add_to_playlist)
	private val stop = itemView.findViewById(R.id.b_stop)
	private val settings = itemView.findViewById(R.id.b_settings)
	private val timePosition = itemView.findViewById(R.id.sb_progress) as SeekBar

	private var player: MediaPlayerController? = null

	init
	{
		this.name.onTextEditedListener = this
		this.play.setOnClickListener(this)
		this.loop.setOnClickListener(this)
		this.inPlaylist.setOnClickListener(this)
		this.stop.setOnClickListener(this)
		this.settings.setOnClickListener(this)
		this.timePosition.setOnSeekBarChangeListener(this)
	}

	fun bindData(data: MediaPlayerController)
	{
		this.player = data

		this.updateViewToPlayerState()
	}

	private fun updateViewToPlayerState()
	{
		val player = this.player as MediaPlayerController
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
		this.player?.apply {
			if (!this.isDeletionPending)
				timePosition.progress = progress
		}
	}

	override fun onTextEdited(text: String)
	{
		Logger.d(TAG, "onTextEdited: $text")

		this.name.clearFocus()
		if (this.player != null)
		{
			val playerData = this.player!!.mediaPlayerData
			val currentLabel = playerData.label

			if (!currentLabel.equals(text))
			{
				playerData.label = text
				playerData.updateItemInDatabaseAsync()

				this.eventBus.post(OpenSoundRenameEvent(playerData))
			}
		}
	}

	override fun onClick(view: View)
	{
		val player = this.player as MediaPlayerController

		val isSelected = view.isSelected
		val id = view.id
		when (id) {
			R.id.b_stop ->
			{
				player.stopSound()
				this.updateViewToPlayerState()
			}
			R.id.b_loop ->
			{
				view.isSelected = !isSelected
				player.isLoopingEnabled = !isSelected
			}
			R.id.b_add_to_playlist ->
			{
				view.isSelected = !isSelected
				player.isInPlaylist = !isSelected
				player.mediaPlayerData.updateItemInDatabaseAsync()
				soundsDataStorage.toggleSoundInPlaylist(player.mediaPlayerData.playerId, !isSelected)
			}
			R.id.b_play -> {
				name.clearFocus()
					if (!isSelected)
					{
						this.progressTimer.startProgressUpdateTimer()
						player.playSound()
					}
					else
					{
						player.fadeOutSound()
					}
			}
			R.id.b_settings -> {
				player.pauseSound()
				this.eventBus.post(OpenSoundSettingsEvent(player.mediaPlayerData))
			}
		}
	}

	override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
	{
		if (fromUser)
			this.player?.progress = progress
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

	private fun Handler.startTimerDelayed()
	{
		this.postDelayed({ progressTimer.startProgressUpdateTimer() }, (2 * UPDATE_INTERVAL).toLong())
	}
}

class SoundItemPagerAdapter : PagerAdapter()
{

	override fun getCount(): Int
	{
		return 3 // main sound control + delete left and right
	}

	override fun isViewFromObject(view: View, `object`: Any): Boolean
	{
		return view == `object`
	}

	override fun instantiateItem(container: ViewGroup, position: Int): Any
	{
		if (position == VIEWPAGER_INDEX_SOUND_CONTROLS)
			return container.findViewById(R.id.layout_sound_controls)
		else if (position == VIEWPAGER_INDEX_SOUND_CONTROLS - 1)
			return container.findViewById(R.id.layout_remove_sound_item_left)
		else
			return container.findViewById(R.id.layout_remove_sound_item_right)
	}
}