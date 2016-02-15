package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
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
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeChangeRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OnActionModeChangeRequestedEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OnOpenSoundLayoutsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayouts
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheets
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog

class NavigationDrawerFragment : BaseFragment(),
		View.OnClickListener,
		TabLayout.OnTabSelectedListener,
		OnActionModeChangeRequestedEventListener,
		OnOpenSoundLayoutsEventListener
{
	private val TAG = javaClass.name

	private val INDEX_SOUND_SHEETS = 0
	private val INDEX_PLAYLIST = 1

	private val soundSheetsDataUtil = SoundboardApplication.getSoundSheetsDataUtil()
	private val soundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess()

	private val soundLayoutsUtil = SoundboardApplication.getSoundLayoutsUtil()

	private val eventBus = EventBus.getDefault()

	private val listObserver: ViewPagerContentObserver = ViewPagerContentObserver()

	private var tabBar: TabLayout? = null
	private var tabContent: ViewPager? = null
	private val tabContentAdapter = TabContentAdapter()

	private var listContainer: ViewGroup? = null
	private var soundLayoutList: SoundLayouts? = null
	private var playlist: Playlist? = null
	private var soundSheets: SoundSheets? = null

	private var contextualActionContainer: View? = null
	private var deleteSelected: View? = null

	private var minHeightOfListContent = 0

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		val fragmentView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)

		this.contextualActionContainer = fragmentView.findViewById(R.id.layout_contextual_controls)
		this.listContainer = fragmentView.findViewById(R.id.layout_navigation_drawer_list_content) as ViewGroup

		this.deleteSelected = fragmentView.findViewById(R.id.b_delete_selected)
		this.deleteSelected!!.setOnClickListener(this)

		fragmentView.findViewById(R.id.b_delete).setOnClickListener(this)
		fragmentView.findViewById(R.id.b_ok).setOnClickListener(this)

		val tabContent = fragmentView.findViewById(R.id.vp_tab_content) as ViewPager
		tabContent.adapter = this.tabContentAdapter
		this.tabContent = tabContent

		val tabBar = fragmentView.findViewById(R.id.tl_tab_bar) as TabLayout
		tabBar.setOnTabSelectedListener(this)
		tabBar.setupWithViewPager(tabContent)
		this.tabBar = tabBar

		this.soundLayoutList = fragmentView.findViewById(R.id.layout_select_sound_layout) as SoundLayouts
		this.playlist = fragmentView.findViewById(R.id.playlist) as Playlist
		this.soundSheets = fragmentView.findViewById(R.id.sound_sheets) as SoundSheets

		return fragmentView
	}

	override fun onResume()
	{
		super.onResume()

		this.calculateMinHeightOfListContent()
		this.adjustViewPagerToContent()

		this.playlist!!.adapter.registerAdapterDataObserver(this.listObserver)
		this.soundSheets!!.adapter.registerAdapterDataObserver(this.listObserver)
	}

	/**
	 * Calculates the minimum require height of the viewpager's content (this is the height used if the content is smaller than the
	 * screens height). Recalculation is require every time the screen's metric changes (ie. switch from/to full immersive mode).
	 */
	fun calculateMinHeightOfListContent()
	{
		this.minHeightOfListContent = this.contextualActionContainer!!.top - listContainer!!.top  // this is the minimal height required to fill the screen properly
	}

	override fun onStart()
	{
		super.onStart()
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onStop()
	{
		super.onStop()
		this.eventBus.unregister(this)
	}

	override fun onPause()
	{
		super.onPause()

		this.playlist!!.adapter.unregisterAdapterDataObserver(this.listObserver)
		this.soundSheets!!.adapter.unregisterAdapterDataObserver(this.listObserver)
	}

	override fun onClick(v: View)
	{
		val id = v.id
		if (id == R.id.b_delete)
		{
			if (this.soundLayoutList!!.isActive())
				this.soundLayoutList!!.presenter.prepareItemDeletion()
			else if (this.tabContent!!.currentItem == INDEX_PLAYLIST)
				this.playlist!!.presenter.prepareItemDeletion()
			else
				this.soundSheets!!.presenter.prepareItemDeletion()
		}
		else if (id == R.id.b_delete_selected)
		{
			if (this.soundLayoutList!!.isActive())
				this.soundLayoutList!!.presenter.deleteSelectedItems()
			else if (this.tabContent!!.currentItem == INDEX_PLAYLIST)
				this.playlist!!.presenter.deleteSelectedItems()
			else
				this.soundSheets!!.presenter.deleteSelectedItems()
		}
		else if (id == R.id.b_ok)
		{
			if (this.soundLayoutList!!.isActive())
				AddNewSoundLayoutDialog.showInstance(this.fragmentManager, this.soundLayoutsUtil.getSuggestedName())
			else if (this.tabContent!!.currentItem == INDEX_PLAYLIST)
				AddNewSoundDialog(this.fragmentManager, Playlist.TAG)
			else
				AddNewSoundSheetDialog.showInstance(this.fragmentManager, this.soundSheetsDataUtil.getSuggestedName())
		}
	}

	private fun animateSoundLayoutsListAppear()
	{
		val viewToAnimate = this.activity.findViewById(R.id.v_reveal_shadow)
		val animator = AnimationUtils.createSlowCircularReveal(viewToAnimate, this.listContainer!!.width, 0, 0f, (2 * this.listContainer!!.height).toFloat())

		animator?.start()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutsRequestedEvent)
	{
		this.soundLayoutList!!.toggleVisibility()
		this.animateSoundLayoutsListAppear()
		if (this.baseActivity.isActionModeActive && this.soundLayoutList!!.isActive())
			this.soundLayoutList!!.presenter.prepareItemDeletion()
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

	override fun onTabSelected(selectedTab: TabLayout.Tab)
	{
		if (!this.baseActivity.isActionModeActive)
			return
		val position = selectedTab.position
		if (position == INDEX_SOUND_SHEETS)
			this.soundSheets!!.presenter.prepareItemDeletion()
		else if (position == INDEX_PLAYLIST)
			this.playlist!!.presenter.prepareItemDeletion()
	}

	override fun onTabReselected(p0: TabLayout.Tab?) {}

	override fun onTabUnselected(p0: TabLayout.Tab?) {}

	/**
	 * This function resize the view pagers height to its content. It is necessary, because the viewpager can not
	 * have layout parameter wrap_content.
	 */
	fun adjustViewPagerToContent()
	{
		val resources = SoundboardApplication.context.resources
		val childHeight = resources.getDimensionPixelSize(R.dimen.height_list_item)
		val dividerHeight = resources.getDimensionPixelSize(R.dimen.stroke)
		val padding = resources.getDimensionPixelSize(R.dimen.margin_small)

		val soundSheetCount = this.soundSheetsDataAccess.getSoundSheets().size
		val playListCount = this.playlist!!.adapter.itemCount

		val heightSoundSheetChildren = soundSheetCount * childHeight
		val heightDividerSoundSheet = if (soundSheetCount > 1) (soundSheetCount - 1) * dividerHeight else 0
		val heightSoundSheet = heightSoundSheetChildren + heightDividerSoundSheet + padding + this.tabBar!!.height

		val heightPlayListChildren = playListCount * childHeight
		val heightDividerPlayList = if (playListCount > 1) (playListCount - 1) * dividerHeight else 0
		val heightPlayList = heightPlayListChildren + heightDividerPlayList + padding + this.tabBar!!.height

		val largestList = Math.max(heightSoundSheet, heightPlayList)
		if (this.minHeightOfListContent == 0)
		// 0 means the current height was not measured, remeasure
			this.minHeightOfListContent = this.contextualActionContainer!!.top - listContainer!!.top

		this.listContainer!!.layoutParams.height = Math.max(largestList, minHeightOfListContent)
	}

	private inner class TabContentAdapter : PagerAdapter()
	{
		override fun getPageTitle(position: Int): CharSequence
		{
			if (position == INDEX_SOUND_SHEETS)
				return resources.getString(R.string.tab_sound_sheets)
			else
				return resources.getString(R.string.tab_play_list)
		}

		override fun getCount(): Int
		{
			return 2
		}

		override fun isViewFromObject(view: View, `object`: Any): Boolean
		{
			return view == `object`
		}

		override fun instantiateItem(container: ViewGroup, position: Int): Any
		{
			when (position) {
				INDEX_SOUND_SHEETS -> return soundSheets as SoundSheets
				INDEX_PLAYLIST -> return playlist as Playlist
				else -> throw NullPointerException("instantiateItem: no view for position $position is available")
			}
		}
	}

	private inner class ViewPagerContentObserver : RecyclerView.AdapterDataObserver()
	{
		override fun onChanged()
		{
			super.onChanged()
			adjustViewPagerToContent()
		}
	}
}
