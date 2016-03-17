package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
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
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils
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
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

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

		val tabLayout = (view.findViewById(R.id.tl_tab_bar) as TabLayout)

		val layoutList = (view.findViewById(R.id.rv_navigation_drawer_list) as RecyclerView).apply {
			this.itemAnimator = DefaultItemAnimator()
			this.layoutManager = LinearLayoutManager(this.context)
			this.addItemDecoration(DividerItemDecoration(this.context))
		}

		this.presenter = NavigationDrawerFragmentPresenter(
				eventBus = this.eventBus,

				coordinatorLayout = view.findViewById(R.id.cl_navigation_drawer) as NonTouchableCoordinatorLayout,

				toolbar = view.findViewById(R.id.toolbar_navigation_drawer) as Toolbar,
				appBarLayout = view.findViewById(R.id.abl_navigation_drawer) as AppBarLayout,
				tabLayout = tabLayout,
				buttonOk = view.findViewById(R.id.b_ok),
				buttonDelete = view.findViewById(R.id.b_delete),
				buttonDeleteSelected = view.findViewById(R.id.b_delete_selected),
				buttonCancelActionMode = view.findViewById(R.id.b_cancel_action_mode),
				buttonSelectAll = view.findViewById(R.id.ll_action_mode_title),

				actionModeTitle = view.findViewById(R.id.tv_action_mode_title) as TextView,
				actionModeSubTitle= view.findViewById(R.id.tv_action_mode_sub_title) as TextView,

				revealShadow = view.findViewById(R.id.v_reveal_shadow),

				fragmentManager = this.fragmentManager,
				recyclerView = layoutList,

				soundsDataAccess = SoundboardApplication.getSoundsDataAccess(),
				soundsDataStorage = SoundboardApplication.getSoundsDataStorage(),

				soundSheetsDataUtil = SoundboardApplication.getSoundSheetsDataUtil(),
				soundSheetsDataStorage = SoundboardApplication.getSoundSheetsDataStorage(),
				soundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess(),

				soundLayoutsAccess = SoundboardApplication.getSoundLayoutsAccess(),
				soundLayoutsStorage = SoundboardApplication.getSoundLayoutsStorage(),
				soundLayoutsUtil = SoundboardApplication.getSoundLayoutsUtil()

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

private val INDEX_SOUND_SHEETS = 0
private val INDEX_PLAYLIST = 1

class NavigationDrawerFragmentPresenter
(
		private val eventBus: EventBus,
		private val fragmentManager: FragmentManager,

		private val coordinatorLayout: NonTouchableCoordinatorLayout,

		private val toolbar: Toolbar,
		private val appBarLayout: AppBarLayout,
		private val tabLayout: TabLayout,
		private val revealShadow: View,
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
		private val soundSheetsDataUtil: SoundSheetsDataUtil

) :
		View.OnClickListener,
		ItemSelectedForDeletionListener,
		OnOpenSoundLayoutsEventListener,
		TabLayout.OnTabSelectedListener,
		OnSoundLayoutSelectedEventListener
{
	private var tabSoundSheets: TabLayout.Tab = tabLayout.createSoundSheetTab()
	private var tabPlayList: TabLayout.Tab = tabLayout.createPlaylistTab()
	private var tabSoundLayouts: TabLayout.Tab = tabLayout.createSoundLayoutsTab()

	private var tabMode: TabMode = TabMode.Normal

	private var currentList: List = List.SoundSheet
	private var currentPresenter: NavigationDrawerListPresenter? = null

	private val presenterSoundSheets = createSoundSheetPresenter(eventBus, recyclerView, soundsDataAccess, soundsDataStorage, soundSheetsDataAccess, soundSheetsDataStorage)
	private val presenterPlaylist = createPlaylistPresenter(eventBus, recyclerView, soundsDataAccess, soundsDataStorage)
	private val presenterSoundLayouts = createSoundLayoutsPresenter(eventBus, recyclerView, soundLayoutsAccess, soundLayoutsStorage)

	init
	{
		this.tabLayout.setOnTabSelectedListener(this)
		this.buttonOk.setOnClickListener(this)
		this.buttonDelete.setOnClickListener(this)
		this.buttonDeleteSelected.setOnClickListener(this)
		this.buttonCancelActionMode.setOnClickListener(this)
		this.buttonSelectAll.setOnClickListener(this)
	}

	fun onAttachedToWindow()
	{
		this.showDefaultTabBarAndContent()

		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	fun onDetachedFromWindow(): Unit = this.eventBus.unregister(this)

	private fun showDefaultTabBarAndContent()
	{
		this.tabMode = TabMode.Normal
		this.tabLayout.removeAllTabs()
		this.tabSoundSheets = this.tabLayout.createSoundSheetTab()
		this.tabPlayList = this.tabLayout.createPlaylistTab()

		this.tabLayout.addTab(this.tabSoundSheets, INDEX_SOUND_SHEETS)
		this.tabLayout.addTab(this.tabPlayList, INDEX_PLAYLIST)

		this.tabSoundSheets.select()
	}

	private fun showContextTabBarAndContent()
	{
		this.tabMode = TabMode.Context
		this.tabLayout.removeAllTabs()
		this.tabSoundLayouts = this.tabLayout.createSoundLayoutsTab()

		this.tabLayout.addTab(this.tabSoundLayouts)
		this.tabSoundLayouts.select()
	}

	private fun showToolbarForDeletion()
	{
		this.appBarLayout.setExpanded(false, true)
		this.coordinatorLayout.isScrollingEnabled = false
		this.recyclerView.isNestedScrollingEnabled = false
		this.toolbar.visibility = View.VISIBLE

		val distance = this.recyclerView.width
		this.buttonDeleteSelected.apply {
			this.visibility = View.VISIBLE
			this.translationX = (-distance).toFloat()
			animate()
					.translationX(0f)
					.setDuration(this.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
					.setInterpolator(DecelerateInterpolator())
					.start()
		}
	}

	private fun hideToolbarForDeletion()
	{
		this.toolbar.visibility = View.GONE
		this.appBarLayout.setExpanded(true, true)
		this.coordinatorLayout.isScrollingEnabled = true
		this.recyclerView.isNestedScrollingEnabled = true
		this.recyclerView.scrollToPosition(0)

		this.buttonDeleteSelected.visibility = View.GONE
	}

	override fun onClick(view: View)
	{
		val id = view.id
		when (id)
		{
			this.buttonDelete.id ->
			{
				val itemCount = this.currentPresenter?.itemCount ?: 0
				if (itemCount == 0) return
				this.showToolbarForDeletion()
				this.currentPresenter?.startDeletionMode()
				this.setActionModeSubTitle(0, itemCount)
			}
			this.buttonDeleteSelected.id ->
			{
				this.currentPresenter?.deleteSelectedItems()
				this.currentPresenter?.stopDeletionMode()
				this.hideToolbarForDeletion()
			}
			this.buttonCancelActionMode.id ->
			{
				this.currentPresenter?.stopDeletionMode()
				this.hideToolbarForDeletion()
			}
			this.buttonSelectAll.id ->
			{
				this.currentPresenter?.selectAllItems()
				val itemCount = this.currentPresenter?.itemCount ?: 0
				this.setActionModeSubTitle(itemCount, itemCount)
			}
			this.buttonOk.id ->
				if (this.currentList == List.SoundLayouts)
				{
					AddNewSoundLayoutDialog.showInstance(this.fragmentManager, this.soundLayoutsUtil.getSuggestedName())
				}
				else if (this.currentList == List.Playlist)
				{
					AddNewSoundDialog(this.fragmentManager, PlaylistTAG)
				}
				else if (this.currentList == List.SoundSheet)
				{
					AddNewSoundSheetDialog.showInstance(this.fragmentManager, this.soundSheetsDataUtil.getSuggestedName())
				}
		}
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: ItemSelectedForDeletion)
	{
		this.setActionModeSubTitle(event.selectedItemCount, event.itemCount)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	private fun setActionModeSubTitle(count: Int, maxValue: Int)
	{
		var countString = Integer.toString(count)
		if (countString.length == 1)
			countString = " " + countString
		countString = countString + "/" + maxValue
		this.actionModeSubTitle.text = countString
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutsRequestedEvent)
	{
		if (event.openSoundLayouts) {
			this.showContextTabBarAndContent()
			this.animateSoundLayoutsListAppear()
		}
		else {
			this.showDefaultTabBarAndContent()
			this.animateSoundLayoutsListAppear()
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent)
	{
		this.showDefaultTabBarAndContent()
	}

	private fun animateSoundLayoutsListAppear()
	{
		if (this.revealShadow.isAttachedToWindow)
		{
			val animator = AnimationUtils.createSlowCircularReveal(this.revealShadow, this.recyclerView.width, 0, 0f, (2 * this.recyclerView.height).toFloat())
			animator?.start()
		}
	}

	override fun onTabReselected(tab: TabLayout.Tab?) {}
	override fun onTabUnselected(tab: TabLayout.Tab?) {}
}

private fun TabLayout.createSoundSheetTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_sound_sheets)

private fun TabLayout.createPlaylistTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_play_list)

private fun TabLayout.createSoundLayoutsTab(): TabLayout.Tab = this.newTab().setText(R.string.navigation_drawer_select_sound_layout)
