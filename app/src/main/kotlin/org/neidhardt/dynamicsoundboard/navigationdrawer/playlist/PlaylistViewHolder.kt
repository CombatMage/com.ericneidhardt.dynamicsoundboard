package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressViewHolder

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
public class PlaylistViewHolder
(
		itemView: View,
		private val onItemClickListener: NavigationDrawerItemClickListener<EnhancedMediaPlayer>
) :
		RecyclerView.ViewHolder(itemView),
		SoundProgressViewHolder
{

	private val label = itemView.findViewById(R.id.tv_label) as TextView
	private val selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView
	private val timePosition = itemView.findViewById(R.id.sb_progress) as SeekBar

	private var player: EnhancedMediaPlayer? = null

	init
	{
		itemView.setOnClickListener( { view -> this.onItemClickListener.onItemClick(this.player as EnhancedMediaPlayer) })
	}

	public fun bindData(player: EnhancedMediaPlayer)
	{
		this.player = player

		this.timePosition.setMax(player.getDuration())
		this.label.setText(player.getMediaPlayerData().getLabel())
		this.selectionIndicator.setVisibility(if (player.isPlaying()) View.VISIBLE else View.INVISIBLE)

		this.label.setActivated(player.getMediaPlayerData().getIsSelectedForDeletion())
		this.itemView.setSelected(player.getMediaPlayerData().getIsSelectedForDeletion())

		this.onProgressUpdate()
	}

	override fun onProgressUpdate()
	{
		if (player?.isPlaying() ?: false)
		{
			timePosition.setProgress(player!!.getCurrentPosition())
			timePosition.setVisibility(View.VISIBLE)
		}
		else
			timePosition.setVisibility(View.INVISIBLE)
	}

}