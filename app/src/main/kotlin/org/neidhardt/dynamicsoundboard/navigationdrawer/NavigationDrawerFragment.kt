package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.databinding.FragmentNavigationDrawerBinding
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerButtonBarVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerDeletionViewVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerHeaderVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.*
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.List
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.OnSoundLayoutsChangedEventListener
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutsRemovedEvent
import org.neidhardt.utils.letThis
import org.neidhardt.eventbus_utils.registerIfRequired
import kotlin.properties.Delegates

class NavigationDrawerFragment : BaseFragment(), OnSoundLayoutsChangedEventListener {

	override var fragmentTag: String = javaClass.name

	private val eventBus = EventBus.getDefault()

	private val soundLayoutAccess = SoundboardApplication.soundLayoutsAccess

	private var tabView: NavigationDrawerTabLayout? = null
	private var listView: NavigationDrawerListLayout? = null

	private var binding by Delegates.notNull<FragmentNavigationDrawerBinding>()
	private val headerVM = NavigationDrawerHeaderVM(this.eventBus, this.soundLayoutAccess.getActiveSoundLayout().label)
	private val buttonBarVM = NavigationDrawerButtonBarVM()
	private val deletionViewVM = NavigationDrawerDeletionViewVM()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		this.binding = DataBindingUtil.inflate<FragmentNavigationDrawerBinding>(inflater,
				R.layout.fragment_navigation_drawer, container, false).letThis {
			it.layoutNavigationDrawerHeader.viewModel = this.headerVM
			it.layoutNavigationDrawerButtonBar.viewModel = this.buttonBarVM
			it.layoutNavigationDrawerDeletionHeader.viewModel = this.deletionViewVM
		}
		return this.binding.root
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		this.listView = NavigationDrawerListPresenter(
			eventBus = this.eventBus,
				coordinatorLayout = this.cl_navigation_drawer,
				deletionViewVM = this.deletionViewVM,
				appBarLayout = this.abl_navigation_drawer,
				buttonBarVM = this.buttonBarVM,
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

		this.eventBus.registerIfRequired(this)
		this.tabView?.onAttached()
		this.listView?.onAttached()
	}

	override fun onStop() {
		super.onStop()

		this.eventBus.unregister(this)
		this.tabView?.onDetached()
		this.listView?.onDetached()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutsRemovedEvent) {
		this.headerVM.title = this.soundLayoutAccess.getActiveSoundLayout().label
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutRenamedEvent) {
		this.headerVM.title = this.soundLayoutAccess.getActiveSoundLayout().label
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent) {
		this.headerVM.title = this.soundLayoutAccess.getActiveSoundLayout().label
		this.headerVM.isSoundLayoutOpen = false
	}
}

