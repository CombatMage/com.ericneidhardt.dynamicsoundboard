package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletion
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletionListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OnOpenSoundLayoutsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.createPlaylistPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.createSoundLayoutsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.createSoundSheetPresenter
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.OnSoundLayoutSelectedEventListener
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsUtil
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.NonTouchableCoordinatorLayout

class NavigationDrawerFragment : BaseFragment()
{
	private val eventBus = EventBus.getDefault()
	private var presenter: NavigationDrawerFragmentPresenter? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		val view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)

		val layoutList = (view.findViewById(R.id.rv_navigation_drawer_list) as RecyclerView).apply {
			this.itemAnimator = DefaultItemAnimator()
			this.layoutManager = LinearLayoutManager(this.context)
		}

		this.presenter = NavigationDrawerFragmentPresenter(
				eventBus = this.eventBus,

				coordinatorLayout = view.findViewById(R.id.cl_navigation_drawer) as NonTouchableCoordinatorLayout,

				appBarLayout = view.findViewById(R.id.abl_navigation_drawer) as AppBarLayout,
				toolbarDeletion = view.findViewById(R.id.v_navigation_drawer_deletion) as View,
				tabLayout = view.findViewById(R.id.tl_navigation_drawer_list) as TabLayout,

				buttonOk = view.findViewById(R.id.b_ok),
				buttonDelete = view.findViewById(R.id.b_delete),
				buttonDeleteSelected = view.findViewById(R.id.b_delete_selected),
				buttonCancelActionMode = view.findViewById(R.id.b_cancel_action_mode),
				buttonSelectAll = view.findViewById(R.id.ll_action_mode_title),

				actionModeTitle = view.findViewById(R.id.tv_action_mode_title) as TextView,
				actionModeSubTitle= view.findViewById(R.id.tv_action_mode_sub_title) as TextView,

				fragmentManager = this.fragmentManager,
				recyclerView = layoutList,

				soundsDataAccess = SoundboardApplication.soundsDataAccess,
				soundsDataStorage = SoundboardApplication.soundsDataStorage,

				soundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil,
				soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage,
				soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess,

				soundLayoutsAccess = SoundboardApplication.soundLayoutsAccess,
				soundLayoutsStorage = SoundboardApplication.soundLayoutsStorage,
				soundLayoutsUtil = SoundboardApplication.soundLayoutsUtil

		).apply {
			onAttachedToWindow()
		}

		return view
	}

	override fun onStart()
	{
		super.onStart()
		this.presenter?.onAttachedToWindow()
	}

	override fun onStop() {
		super.onStop()
		this.presenter?.onDetachedFromWindow()
	}
}

enum class List
{
	SoundSheet,
	Playlist,
	SoundLayouts
}

enum class TabMode
{
	Normal,
	Context
}

class NavigationDrawerFragmentPresenter
(
		private val eventBus: EventBus,
		private val fragmentManager: FragmentManager,

		private val coordinatorLayout: NonTouchableCoordinatorLayout,

		private val appBarLayout: AppBarLayout,
		private val toolbarDeletion: View,
		private val tabLayout: TabLayout,
		private val buttonOk: View,
		private val buttonDelete: View,
		private val buttonDeleteSelected: View,
		private val buttonCancelActionMode: View,
		private val buttonSelectAll: View,
		private val actionModeTitle: TextView,
		private val actionModeSubTitle: TextView,
		private val recyclerView: RecyclerView,

		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundLayoutsStorage: SoundLayoutsStorage,
		private val soundLayoutsUtil: SoundLayoutsUtil,

		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage,

		private val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,


) :
		View.OnClickListener,
		ItemSelectedForDeletionListener,
		OnOpenSoundLayoutsEventListener,
		TabLayout.OnTabSelectedListener,
		OnSoundLayoutSelectedEventListener
{

	init
	{
		this.tabLayout.setOnTabSelectedListener(this)

	}

	fun onAttachedToWindow()
	{
		this.showDefaultTabBarAndContent()
		this.eventBus.registerIfRequired(this)
	}

	fun onDetachedFromWindow(): Unit = this.eventBus.unregister(this)

	private fun showDefaultTabBarAndContent()
	{

	}

	private fun showContextTabBarAndContent()
	{

	}




	override fun onTabSelected(tab: TabLayout.Tab?)
	{
		this.currentPresenter?.onDetachedFromWindow()

		if (this.tabMode == TabMode.Context)
		{
			this.currentList = List.SoundLayouts
			this.currentPresenter = this.presenterSoundLayouts
			this.recyclerView.adapter = this.presenterSoundLayouts.adapter
			this.actionModeTitle.setText(R.string.cab_title_delete_sound_layouts)
		}
		else if (tabMode == TabMode.Normal)
		{
			when (tab)
			{
				this.tabSoundSheets -> {
					this.currentList = List.SoundSheet
					this.currentPresenter = this.presenterSoundSheets
					this.recyclerView.adapter = this.presenterSoundSheets.adapter
					this.actionModeTitle.setText(R.string.cab_title_delete_sound_sheets)
				}
				this.tabPlayList -> {
					this.currentList = List.Playlist
					this.currentPresenter = this.presenterPlaylist
					this.recyclerView.adapter = this.presenterPlaylist.adapter
					this.actionModeTitle.setText(R.string.cab_title_delete_play_list_sounds)
				}
			}
		}
		this.currentPresenter?.onAttachedToWindow()
	}

	override fun onTabReselected(tab: TabLayout.Tab?) {}
	override fun onTabUnselected(tab: TabLayout.Tab?) {}
}

