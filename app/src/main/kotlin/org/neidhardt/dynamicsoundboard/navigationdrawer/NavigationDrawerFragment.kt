package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.app.FragmentManager
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OnActionModeChangeRequestedEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OnOpenSoundLayoutsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayouts
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheets
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsUtil
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

private val INDEX_SOUND_SHEETS = 0
private val INDEX_PLAYLIST = 1

class NavigationDrawerFragment : BaseFragment(),
		OnActionModeChangeRequestedEventListener
{
	private val TAG = javaClass.name

	private val soundSheetsDataUtil = SoundboardApplication.getSoundSheetsDataUtil()
	private val soundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess()

	private val soundLayoutsUtil = SoundboardApplication.getSoundLayoutsUtil()

	private val eventBus = EventBus.getDefault()

	private var listContainer: ViewGroup? = null
	private var soundLayouts: SoundLayouts? = null
	private var playlist: Playlist? = null
	private var soundSheets: SoundSheets? = null

	private var contextualActionContainer: View? = null
	private var deleteSelected: View? = null

	private var presenter: NavigationDrawerFragmentPresenter? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		val view = inflater.inflate(R.layout.fragment_navigation_drawer_rebrush, container, false)

		this.contextualActionContainer = view.findViewById(R.id.layout_contextual_controls)

		val tabLayout = (view.findViewById(R.id.tl_tab_bar) as TabLayout).apply {
			this.tabGravity = TabLayout.GRAVITY_FILL
		}

		val layoutList = (view.findViewById(R.id.rv_navigation_drawer_list) as RecyclerView).apply {
			this.itemAnimator = DefaultItemAnimator()
			this.layoutManager = LinearLayoutManager(this.context)
			this.addItemDecoration(DividerItemDecoration(this.context))
		}

		this.presenter = NavigationDrawerFragmentPresenter(
				eventBus = this.eventBus,
				tabLayout = tabLayout,
				buttonOk = view.findViewById(R.id.b_ok),
				buttonDelete = view.findViewById(R.id.b_delete),
				buttonDeleteSelected = view.findViewById(R.id.b_delete_selected),
				fragmentManager = this.fragmentManager,

				recyclerView = layoutList,

				soundsDataAccess = SoundboardApplication.getSoundsDataAccess(),
				soundsDataStorage = SoundboardApplication.getSoundsDataStorage(),

				soundSheetsDataUtil = SoundboardApplication.getSoundSheetsDataUtil(),
				soundSheetsDataStorage = SoundboardApplication.getSoundSheetsDataStorage(),
				soundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess(),

				soundLayoutsUtil = this.soundLayoutsUtil

		).apply {
			onAttachedToWindow()
		}

		return view
	}

	override fun onStart()
	{
		super.onStart()
		this.presenter?.onAttachedToWindow()
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onStop()
	{
		super.onStop()
		this.presenter?.onDetachedFromWindow()
		this.eventBus.unregister(this)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: ActionModeChangeRequestedEvent)
	{
		val requestedAction = event.requestedAction
		when (requestedAction)
		{
			ActionModeChangeRequestedEvent.REQUEST.START -> this.onActionModeStart()
			ActionModeChangeRequestedEvent.REQUEST.STOPPED -> this.onActionModeFinished()
			else -> Logger.d(TAG, event.toString())
		}
	}

	private fun onActionModeStart()
	{
		this.deleteSelected!!.visibility = View.VISIBLE
		val distance = this.contextualActionContainer!!.width

		this.deleteSelected!!.translationX = (-distance).toFloat()
		this.deleteSelected!!.animate().translationX(0f).setDuration(this.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()).setInterpolator(DecelerateInterpolator()).start()
	}

	private fun onActionModeFinished()
	{
		this.deleteSelected!!.visibility = View.GONE
	}
}

enum class List
{
	SoundSheet,
	Playlist,
	SoundLayouts
}

private class NavigationDrawerFragmentPresenter
(
		private val eventBus: EventBus,
		private val fragmentManager: FragmentManager,
		private val tabLayout: TabLayout,
		private val buttonOk: View,
		private val buttonDelete: View,
		private val buttonDeleteSelected: View,

		private val recyclerView: RecyclerView,

		private val soundLayoutsUtil: SoundLayoutsUtil,

		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage,

		private val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,
		private val soundSheetsDataUtil: SoundSheetsDataUtil

) :
		View.OnClickListener,
		OnOpenSoundLayoutsEventListener,
		TabLayout.OnTabSelectedListener
{
	private var tabSoundSheets: TabLayout.Tab = tabLayout.createSoundSheetTab()
	private var tabPlayList: TabLayout.Tab = tabLayout.createPlaylistTab()
	private var tabSoundLayouts: TabLayout.Tab = tabLayout.createSoundLayoutsTab()

	private var currentList: List = List.SoundSheet
	private var currentPresenter: NavigationDrawerListPresenter? = null

	private val presenterSoundSheets = SoundSheetsPresenter(
			eventBus = this.eventBus,
			soundsDataAccess = this.soundsDataAccess,
			soundsDataStorage = this.soundsDataStorage,
			soundSheetsDataAccess = this.soundSheetsDataAccess,
			soundSheetsDataStorage = this.soundSheetsDataStorage
	)
	private val adapterSoundSheets = SoundSheetsAdapter(this.presenterSoundSheets)

	init
	{
		this.tabLayout.setOnTabSelectedListener(this)
		this.buttonOk.setOnClickListener(this)
		this.buttonDelete.setOnClickListener(this)
		this.buttonDeleteSelected.setOnClickListener(this)
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
		this.tabLayout.removeAllTabs()
		this.tabSoundSheets = this.tabLayout.createSoundSheetTab()
		this.tabPlayList = this.tabLayout.createPlaylistTab()

		this.tabLayout.addTab(this.tabSoundSheets, INDEX_SOUND_SHEETS)
		this.tabLayout.addTab(this.tabPlayList, INDEX_PLAYLIST)

		this.tabSoundSheets.select()
	}

	private fun showContextTabBarAndContent()
	{
		this.tabLayout.removeAllTabs()
		this.tabSoundLayouts = this.tabLayout.createSoundLayoutsTab()

		this.tabLayout.addTab(this.tabSoundLayouts)
		this.tabSoundLayouts.select()
	}

	override fun onClick(view: View)
	{
		val id = view.id
		when (id)
		{
			this.buttonDelete.id -> this.currentPresenter?.prepareItemDeletion()
			this.buttonDeleteSelected.id -> this.currentPresenter?.deleteSelectedItems()
			this.buttonOk.id ->
				if (this.currentList == List.SoundLayouts)
				{
					AddNewSoundLayoutDialog.showInstance(this.fragmentManager, this.soundLayoutsUtil.getSuggestedName())
				}
				else if (this.currentList == List.Playlist)
				{
					AddNewSoundDialog(this.fragmentManager, Playlist.TAG)
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

		when (tab)
		{
			this.tabSoundSheets ->
			{
				this.currentList = List.SoundSheet
				this.currentPresenter = this.presenterSoundSheets
				this.recyclerView.adapter = this.adapterSoundSheets
			}
			this.tabPlayList ->
			{
				this.currentList = List.Playlist
			}
			this.tabSoundLayouts ->
			{
				this.currentList = List.SoundLayouts
			}
		}

		this.currentPresenter?.onAttachedToWindow()
		this.animateSoundLayoutsListAppear()
	}

	private fun animateSoundLayoutsListAppear()
	{
		/*val viewToAnimate = this.activity.findViewById(R.id.v_reveal_shadow)
		val animator = AnimationUtils.createSlowCircularReveal(viewToAnimate, this.listContainer!!.width, 0, 0f, (2 * this.listContainer!!.height).toFloat())

		animator?.start()*/
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutsRequestedEvent)
	{
		if (event.openSoundLayouts) {
			this.showContextTabBarAndContent()
		}
		else {
			this.showDefaultTabBarAndContent()
		}
	}

	override fun onTabReselected(tab: TabLayout.Tab?) {}
	override fun onTabUnselected(tab: TabLayout.Tab?) {}
}

private fun TabLayout.createSoundSheetTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_sound_sheets)

private fun TabLayout.createPlaylistTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_play_list)

private fun TabLayout.createSoundLayoutsTab(): TabLayout.Tab = this.newTab().setText(R.string.navigation_drawer_select_sound_layout)
