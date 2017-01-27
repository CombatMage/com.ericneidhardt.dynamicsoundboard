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
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.databinding.FragmentNavigationDrawerBinding
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.manager.*
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.PlaylistAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerButtonBarVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerDeletionViewVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerHeaderVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.*
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.List
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.utils.letThis
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

class NavigationDrawerFragment : BaseFragment() {

	override var fragmentTag: String = javaClass.name

	private val eventBus = EventBus.getDefault()
	private var subscriptions = CompositeSubscription()

	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val playlistManager = SoundboardApplication.playlistManager

	private var tabView: NavigationDrawerTabLayout? = null
	private var listPresenter: NavigationDrawerListPresenter? = null

	private var binding by Delegates.notNull<FragmentNavigationDrawerBinding>()
	private val headerVM = NavigationDrawerHeaderVM(this.eventBus, this.soundLayoutManager.soundLayouts.activeLayout.label)
	private val buttonBarVM = NavigationDrawerButtonBarVM()
	private val deletionViewVM = NavigationDrawerDeletionViewVM()

	val adapterSoundSheets = SoundSheetsAdapter()
	val adapterPlaylist = PlaylistAdapter()
	val adapterSoundLayouts = SoundLayoutsAdapter()

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

		this.listPresenter = NavigationDrawerListPresenter(
				eventBus = this.eventBus,
				coordinatorLayout = this.cl_navigation_drawer,
				deletionViewVM = this.deletionViewVM,
				appBarLayout = this.abl_navigation_drawer,
				buttonBarVM = this.buttonBarVM,
				recyclerView = this.rv_navigation_drawer_list.apply {
						this.itemAnimator = DefaultItemAnimator()
						this.layoutManager = LinearLayoutManager(this.context)
				},
				fragment = this
		)

		this.tabView = NavigationDrawerTabLayoutPresenter(
				eventBus = this.eventBus,
				tabLayout = this.tl_navigation_drawer_list,
				onPlaylistSelectedCallback = { this.listPresenter?.currentList = List.Playlist },
				onSoundSheetsSelectedCallback = { this.listPresenter?.currentList = List.SoundSheet },
				onSoundLayoutsSelectedCallback = { this.listPresenter?.currentList = List.SoundLayouts }
		)
	}

	override fun onStart() {
		super.onStart()

		this.headerVM.title = this.soundLayoutManager.soundLayouts.activeLayout.label

		this.tabView?.onAttached()
		this.listPresenter?.onAttached()

		this.subscriptions = CompositeSubscription()
		this.subscriptions.addAll(

				RxNewSoundLayoutManager.soundLayoutsChanges(this.soundLayoutManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { layouts ->
							val selectedLayout = layouts.activeLayout
							this.headerVM.title = selectedLayout.label
							this.adapterSoundLayouts.notifyDataSetChanged()
						},

				this.adapterSoundLayouts.clicksSettings
						.subscribe { viewHolder ->
							viewHolder.data?.let { soundLayout ->
								GenericRenameDialogs.showRenameSoundLayoutDialog(this.fragmentManager, soundLayout) }
						},

				this.adapterSoundLayouts.clicksViewHolder
						.subscribe { viewHolder ->
							viewHolder.data?.let { soundLayout ->
								this.listPresenter?.userClicksSoundLayout(soundLayout)
							}
						},

				RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
						.subscribe {
							this.adapterSoundSheets.notifyDataSetChanged()
						},

				RxSoundManager.changesSoundList(this.soundManager)
						.subscribe {
							this.adapterSoundSheets.notifyDataSetChanged()
						},

				RxNewPlaylistManager.playlistChanges(this.playlistManager)
						.subscribe {
							this.adapterPlaylist.notifyDataSetChanged()
						}
		)
	}

	override fun onStop() {
		super.onStop()

		this.subscriptions.unsubscribe()
		this.headerVM.isSoundLayoutOpen = false
		this.tabView?.onDetached()
		this.listPresenter?.onDetached()
	}

	fun onNavigationDrawerClosed() {
		if (this.tabView?.tabMode == TabMode.Context) {
			this.tabView?.showDefaultTabBar()
			this.headerVM.isSoundLayoutOpen = false
		}
	}
}

