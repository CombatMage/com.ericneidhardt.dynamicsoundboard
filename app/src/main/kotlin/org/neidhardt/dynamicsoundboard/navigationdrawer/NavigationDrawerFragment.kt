package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*
import kotlinx.android.synthetic.main.layout_navigation_drawer_button_bar.*
import kotlinx.android.synthetic.main.layout_navigation_drawer_deletion_header.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.*
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.List
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment

class NavigationDrawerFragment : BaseFragment()
{
	private val eventBus = EventBus.getDefault()

	private var tabView: NavigationDrawerTabLayout? = null
	private var listView: NavigationDrawerListLayout? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
			= inflater.inflate(R.layout.fragment_navigation_drawer, container, false)

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		this.listView = NavigationDrawerListPresenter(
			eventBus = this.eventBus,
				coordinatorLayout = this.cl_navigation_drawer,

				actionModeSubTitle = this.tv_layout_navigation_drawer_deletion_header_sub_title,
				actionModeTitle = this.tv_layout_navigation_drawer_deletion_header_title,
				appBarLayout = this.abl_navigation_drawer,
				toolbarDeletion = this.view_navigation_drawer_deletion_header,

				buttonCancelActionMode = this.b_layout_navigation_drawer_deletion_header_cancel,
				buttonDelete = this.b_layout_navigation_drawer_button_bar_delete,
				buttonDeleteSelected = this.b_layout_navigation_drawer_button_bar_delete_selected,
				buttonOk = this.b_layout_navigation_drawer_button_bar_add,
				buttonSelectAll = this.ll_layout_navigation_drawer_deletion_header_title,

				recyclerView = this.rv_navigation_drawer_list.apply {
						this.itemAnimator = DefaultItemAnimator()
						this.layoutManager = LinearLayoutManager(this.context)
				},

				fragmentManager = this.fragmentManager,

				soundLayoutsAccess = SoundboardApplication.soundLayoutsAccess,
				soundLayoutsStorage = SoundboardApplication.soundLayoutsStorage,
				soundLayoutsUtil = SoundboardApplication.soundLayoutsUtil,
				soundsDataAccess = SoundboardApplication.soundsDataAccess,
				soundsDataStorage = SoundboardApplication.soundsDataStorage,
				soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess,
				soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage,
				soundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil
		)

		this.tabView = NavigationDrawerTabLayoutPresenter(
				eventBus = this.eventBus,
				tabLayout = this.tl_navigation_drawer_list,
				onPlaylistSelectedCallback = { this.listView?.currentList = List.Playlist },
				onSoundSheetsSelectedCallback = { this.listView?.currentList = List.SoundSheet },
				onSoundLayoutsSelectedCallback = { this.listView?.currentList = List.SoundLayouts }
		)
	}

	override fun onStart()
	{
		super.onStart()

		this.tabView?.onAttached()
		this.listView?.onAttached()
	}

	override fun onStop() {
		super.onStop()
		this.tabView?.onDetached()
		this.listView?.onDetached()
	}
}

