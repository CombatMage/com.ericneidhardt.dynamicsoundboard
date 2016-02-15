package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
class Playlist : NavigationDrawerList
{
	companion object
	{
		val TAG: String = Playlist::class.java.name
	}

	private val soundsDataStorage = SoundboardApplication.getSoundsDataStorage()
	private val soundsDataAccess = SoundboardApplication.getSoundsDataAccess()

	val presenter: PlaylistPresenter = PlaylistPresenter(EventBus.getDefault(), this.soundsDataStorage, this.soundsDataAccess)
	val adapter:PlaylistAdapter = PlaylistAdapter(this.presenter)

	@SuppressWarnings("unused") constructor(context: Context) : super(context)
	{
		this.init(context)
	}

	@SuppressWarnings("unused") constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context)
	}

	@SuppressWarnings("unused") constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
	{
		this.init(context)
	}

	private fun init(context: Context)
	{
		this.presenter.adapter = this.adapter

		LayoutInflater.from(context).inflate(R.layout.view_playlist, this, true)

		val playlist = this.findViewById(R.id.rv_playlist) as RecyclerView
		if (!this.isInEditMode) {
			playlist.addItemDecoration(DividerItemDecoration(this.context))
			playlist.layoutManager = LinearLayoutManager(this.context)
			playlist.itemAnimator = DefaultItemAnimator()
		}
		playlist.adapter = this.adapter

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

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		this.presenter.view = this
	}

	override val itemCount: Int
		get() = this.adapter.itemCount

	override val actionModeTitle: Int
		get() = R.string.cab_title_delete_play_list_sounds

}
