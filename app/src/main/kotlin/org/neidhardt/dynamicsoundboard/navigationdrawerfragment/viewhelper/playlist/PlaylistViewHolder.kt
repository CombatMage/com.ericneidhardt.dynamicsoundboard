package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.playlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.view_playlist_item.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val label = itemView.tv_label
	private val selectionIndicator = itemView.tv_label
	private val timePosition = itemView.sb_progress
	private val divider = itemView.v_divider

	var player: MediaPlayerController? = null

	fun bindData(player: MediaPlayerController, isLastItem: Boolean) {
		this.player = player

		this.timePosition.visibility = if (player.isPlayingSound) View.VISIBLE else View.INVISIBLE
		this.timePosition.max = player.trackDuration
		this.label.text = player.mediaPlayerData.label
		this.selectionIndicator.visibility = if (player.isPlayingSound) View.VISIBLE else View.INVISIBLE

		this.label.isActivated = player.mediaPlayerData.isSelectedForDeletion
		this.itemView.isSelected = player.mediaPlayerData.isSelectedForDeletion
		this.divider.visibility = if (isLastItem) View.INVISIBLE else View.VISIBLE

		player.setOnProgressChangedEventListener { progress, _ ->
			if (player.isPlayingSound) {
				this.timePosition.max = player.trackDuration
				this.timePosition.progress = progress
				this.timePosition.visibility = View.VISIBLE
			}
			else
				timePosition.visibility = View.INVISIBLE
		}
	}
}