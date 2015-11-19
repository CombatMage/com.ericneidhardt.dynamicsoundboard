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
public class PlaylistAdapter
(
		private val presenter: PlaylistPresenter
) :
		SoundProgressAdapter<PlaylistViewHolder>(),
		NavigationDrawerItemClickListener<MediaPlayerController>
{

	override fun getValues(): List<MediaPlayerController>
	{
		return this.presenter.values
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_playlist_item
	}

	override fun getItemCount(): Int
	{
		return this.getValues().size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
		return PlaylistViewHolder(view, this)
	}

	override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int)
	{
		val data = this.getValues()[position]
		holder.bindData(data)
	}

	override fun onItemClick(data: MediaPlayerController)
	{
		this.presenter.onItemClick(data)
	}
}