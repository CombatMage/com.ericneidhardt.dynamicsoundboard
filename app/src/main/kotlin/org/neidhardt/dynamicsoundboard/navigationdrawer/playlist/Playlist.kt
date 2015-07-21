package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
public class Playlist : NavigationDrawerList
{
	companion object
	{
		public val TAG: String = javaClass<Playlist>().getName()
	}

	private val soundsDataStorage = DynamicSoundboardApplication.getStorage().soundsDataStorage
	private val soundsDataAccess = DynamicSoundboardApplication.getStorage().soundsDataAccess

	public val presenter: PlaylistPresenter = PlaylistPresenter(this.soundsDataStorage, this.soundsDataAccess)
	public val adapter:PlaylistAdapter = PlaylistAdapter(this.presenter)

	SuppressWarnings("unused")
	public constructor(context: Context) : super(context)
	{
		this.init(context)
	}

	SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context)
	}

	SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
	{
		this.init(context)
	}

	private fun init(context: Context)
	{
		this.presenter.adapter = this.adapter

		LayoutInflater.from(context).inflate(R.layout.view_playlist, this, true)

		val playlist = this.findViewById(R.id.rv_playlist) as RecyclerView
		if (!this.isInEditMode()) {
			playlist.addItemDecoration(DividerItemDecoration())
			playlist.setLayoutManager(LinearLayoutManager(context))
			playlist.setItemAnimator(DefaultItemAnimator())
		}
		playlist.setAdapter(this.adapter)

		this.adapter.recyclerView = playlist
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.presenter.onAttachedToWindow()
	}

	override fun onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter.setView(this)
	}

	override fun getActionModeTitle(): Int {
		return R.string.cab_title_delete_play_list_sounds
	}

	override fun getItemCount(): Int
	{
		return this.adapter.getItemCount()
	}

	override fun getPresenter(): NavigationDrawerListPresenter<*>?
	{
		return this.presenter
	}
}
