package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
class PlaylistAdapter
(
		private val presenter: PlaylistPresenter
) :
		SoundProgressAdapter<PlaylistViewHolder>(),
		NavigationDrawerItemClickListener<MediaPlayerController>
{
	init { this.setHasStableIds(true) }

	override fun getItemId(position: Int): Long = this.values[position].mediaPlayerData.playerId.hashCode().toLong()

	override val values: List<MediaPlayerController>
		get() = this.presenter.values

	override fun getItemViewType(position: Int): Int = R.layout.view_playlist_item

	override fun getItemCount(): Int = this.values.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		return PlaylistViewHolder(view, this)
	}

	override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int)
	{
		val data = this.values[position]
		holder.bindData(data, position == this.itemCount - 1)
	}

	override fun onItemClick(data: MediaPlayerController)
	{
		this.presenter.onItemClick(data)
	}
}