package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.playlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val label = itemView.findViewById(R.id.tv_label) as TextView
	private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
	private val timePosition = itemView.findViewById(R.id.sb_progress) as SeekBar
	private val divider = itemView.findViewById(R.id.v_divider)

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