package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetViewHolder
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
public class PlaylistAdapter
(
		private val presenter: PlaylistPresenter
) :
		SoundProgressAdapter<PlaylistViewHolder>(),
		NavigationDrawerItemClickListener<EnhancedMediaPlayer>
{

	override fun getValues(): List<EnhancedMediaPlayer>
	{
		return this.presenter.values
	}

	override fun getItemViewType(position: Int): Int
	{
		return R.layout.view_playlist_item
	}

	override fun getItemCount(): Int
	{
		return this.getValues().size()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder
	{
		val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
		return PlaylistViewHolder(view, this)
	}

	override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int)
	{
		val data = this.getValues().get(position)
		holder.bindData(data)
	}

	override fun onItemClick(data: EnhancedMediaPlayer)
	{
		this.presenter.onItemClick(data)
	}
}