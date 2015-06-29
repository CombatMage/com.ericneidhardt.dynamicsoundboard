package org.neidhardt.dynamicsoundboard.soundcontrol

import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.*

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
private val VIEWPAGER_INDEX_SOUND_CONTROLS = 1

public class SoundViewHolder
(
		itemView: View,
		eventBus: EventBus,
		soundsDataStorage: SoundsDataStorage,
		onItemDelete: (EnhancedMediaPlayer, Int) -> Unit,
		progressTimer: SoundProgressTimer
)
	: DismissibleItemViewHolder<SoundItemPagerAdapter>(itemView, SoundItemPagerAdapter())
	, SoundProgressViewHolder
	, View.OnClickListener
	, CustomEditText.OnTextEditedListener
	, SeekBar.OnSeekBarChangeListener
{
    private val TAG = javaClass.getName()

	private val HEIGHT_LIST_ITEM = itemView.getResources().getDimensionPixelSize(R.dimen.height_list_item_xlarge)
	private val HEIGHT_SHADOW = itemView.getResources().getDimensionPixelSize(R.dimen.height_shadow)

	private val eventBus = eventBus
	private val soundsDataStorage = soundsDataStorage
	private val onItemDelete = onItemDelete
	private val progressTimer = progressTimer

	private val container = itemView
	private val name = itemView.findViewById(R.id.et_name) as CustomEditText
	private val play = itemView.findViewById(R.id.b_play)
	private val loop = itemView.findViewById(R.id.b_loop)
	private val inPlaylist = itemView.findViewById(R.id.b_add_to_playlist)
	private val stop = itemView.findViewById(R.id.b_stop)
	private val settings = itemView.findViewById(R.id.b_settings)
	private val timePosition = itemView.findViewById(R.id.sb_progress) as SeekBar

	private val shadowBottomDeleteViewLeft = itemView.findViewById(R.id.v_shadow_bottom_left)
	private val shadowBottomDeleteViewRight = itemView.findViewById(R.id.v_shadow_bottom_right)
	private val shadowBottom = itemView.findViewById(R.id.v_shadow_bottom)

	private var player: EnhancedMediaPlayer? = null

	init
	{
		this.name.setOnTextEditedListener(this)
		this.play.setOnClickListener(this)
		this.loop.setOnClickListener(this)
		this.inPlaylist.setOnClickListener(this)
		this.stop.setOnClickListener(this)
		this.settings.setOnClickListener(this)
		this.timePosition.setOnSeekBarChangeListener(this)
	}

	public fun bindData(data: EnhancedMediaPlayer)
	{
		this.player = data

		this.showContentPage()

		this.updateViewToPlayerState()
	}

	private fun updateViewToPlayerState()
	{
		val player = this.player as EnhancedMediaPlayer
		val playerData = this.player?.getMediaPlayerData() as MediaPlayerData

		if (!this.name.hasFocus())
			this.name.setText(playerData.getLabel())

		val isPlaying = this.player?.isPlaying() as Boolean
		this.play.setSelected(isPlaying)
		this.loop.setSelected(playerData.getIsLoop())
		this.inPlaylist.setSelected(playerData.getIsInPlaylist())

		this.timePosition.setMax(player.getDuration())
		this.timePosition.setProgress(player.getCurrentPosition())
	}

	public fun showShadowForLastItem(isLastItem: Boolean)
	{
		var shadowViewState = if (isLastItem) View.GONE else View.VISIBLE
		this.shadowBottomDeleteViewLeft.setVisibility(shadowViewState)
		this.shadowBottomDeleteViewRight.setVisibility(shadowViewState)

		shadowViewState = if (isLastItem) View.VISIBLE else View.GONE
		this.shadowBottom.setVisibility(shadowViewState)

		val params = this.container.getLayoutParams()
		params.height =
				if (isLastItem)
					this.HEIGHT_LIST_ITEM + HEIGHT_SHADOW
				else
					this.HEIGHT_LIST_ITEM

		this.container.setLayoutParams(params);
	}

	override fun getIndexOfContentPage(): Int {return VIEWPAGER_INDEX_SOUND_CONTROLS}

	override fun delete()
	{
		if (this.player != null)
			this.onItemDelete(this.player as EnhancedMediaPlayer, this.getLayoutPosition())

		Handler().startTimerDelayed()
	}

	override fun onProgressUpdate()
	{
		if (player != null)
			this.timePosition.setProgress(player!!.getCurrentPosition())
	}

	override fun onTextEdited(newLabel: String?)
	{
		Logger.d(TAG, "onTextEdited: " + newLabel)

		this.name.clearFocus()

		val playerData = this.player?.getMediaPlayerData()
		val currentLabel = playerData?.getLabel()

		if (newLabel?.equals(currentLabel) ?: false)
		{
			playerData?.setLabel(newLabel)
			playerData?.setItemWasAltered()

			this.eventBus.post(OpenSoundRenameEvent(playerData))
		}
	}

	override fun onClick(view: View)
	{
		super<DismissibleItemViewHolder>.onClick(view)

		val player = this.player as EnhancedMediaPlayer

		val isSelected = view.isSelected()
		val id = view.getId()
		when (id) {
			R.id.b_stop ->
			{
				player.stopSound()
				this.updateViewToPlayerState()
			}
			R.id.b_loop ->
			{
				view.setSelected(!isSelected)
				player.setLooping(!isSelected)
			}
			R.id.b_add_to_playlist ->
			{
				view.setSelected(!isSelected)
				player.setIsInPlaylist(!isSelected)
				player.getMediaPlayerData().setItemWasAltered()
				soundsDataStorage.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), !isSelected)
			}
			R.id.b_play -> {
				name.clearFocus()
				view.setSelected(!isSelected)
				if (!isSelected) {
					this.progressTimer.startProgressUpdateTimer()
					player.playSound()
				} else {
					player.fadeOutSound()
				}
			}
			R.id.b_settings -> {
				player.pauseSound()
				this.eventBus.post(OpenSoundSettingsEvent(player.getMediaPlayerData()))
			}
		}
	}

	override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
	{
		if (fromUser)
			this.player?.setPositionTo(progress)
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
		this.postDelayed(Runnable { progressTimer.startProgressUpdateTimer() }, (2 * UPDATE_INTERVAL).toLong())
	}
}

private class SoundItemPagerAdapter : PagerAdapter()
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