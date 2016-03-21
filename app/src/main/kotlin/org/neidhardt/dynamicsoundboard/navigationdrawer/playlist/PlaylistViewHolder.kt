package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressViewHolder

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistViewHolder
(
		itemView: View,
		private val onItemClickListener: NavigationDrawerItemClickListener<MediaPlayerController>
) :
		RecyclerView.ViewHolder(itemView),
		SoundProgressViewHolder
{

	private val label = itemView.findViewById(R.id.tv_label) as TextView
	private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
	private val timePosition = itemView.findViewById(R.id.sb_progress) as SeekBar
	private val divider = itemView.findViewById(R.id.v_divider)

	private var player: MediaPlayerController? = null

	init
	{
		itemView.setOnClickListener( { view -> this.onItemClickListener.onItemClick(this.player as MediaPlayerController) })
	}

	fun bindData(player: MediaPlayerController, isLastItem: Boolean)
	{
		this.player = player

		this.timePosition.max = player.trackDuration
		this.label.text = player.mediaPlayerData.label
		this.selectionIndicator.visibility = if (player.isPlayingSound) View.VISIBLE else View.INVISIBLE

		this.label.isActivated = player.mediaPlayerData.isSelectedForDeletion
		this.itemView.isSelected = player.mediaPlayerData.isSelectedForDeletion
		this.divider.visibility = if (isLastItem) View.INVISIBLE else View.VISIBLE

		this.onProgressUpdate()
	}

	override fun onProgressUpdate()
	{
		this.player?.apply {
			if (isPlayingSound)
			{
				timePosition.progress = progress
				timePosition.visibility = View.VISIBLE
			}
			else
				timePosition.visibility = View.INVISIBLE
		}
	}

}