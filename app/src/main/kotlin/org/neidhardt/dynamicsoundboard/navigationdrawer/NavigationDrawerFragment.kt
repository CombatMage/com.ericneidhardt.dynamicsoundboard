package org.neidhardt.dynamicsoundboard.navigationdrawer

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
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
import org.neidhardt.dynamicsoundboard.view_helper.recyclerview_helper.PaddingDecorator
import org.neidhardt.utils.letThis
import kotlin.properties.Delegates

class NavigationDrawerFragment : BaseFragment() {

	override var fragmentTag: String = javaClass.name

	private val eventBus = EventBus.getDefault()

	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val playlistManager = SoundboardApplication.playlistManager

	private var binding by Delegates.notNull<FragmentNavigationDrawerBinding>()
	private val headerVM = NavigationDrawerHeaderVM(this.eventBus, this.soundLayoutManager.soundLayouts.activeLayout.label)
	private val buttonBarVM = NavigationDrawerButtonBarVM()
	private val deletionViewVM = NavigationDrawerDeletionViewVM()

	val adapterSoundSheets = SoundSheetsAdapter()
	val adapterPlaylist = PlaylistAdapter()
	val adapterSoundLayouts = SoundLayoutsAdapter()

	private var tabView: NavigationDrawerTabLayout? = null
	private var listPresenter: NavigationDrawerListPresenter? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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
				fragment = this,
				recyclerView = this.rv_navigation_drawer_list.apply {
					this.itemAnimator = DefaultItemAnimator()
					this.layoutManager = LinearLayoutManager(this.context)
					this.addItemDecoration(PaddingDecorator(this.context.applicationContext))
				}
		)

		this.buttonBarVM.addClickedCallback = { this.listPresenter?.userClicksAdd() }
		this.buttonBarVM.deleteClickedCallback = { this.listPresenter?.userStartsDeletionMode() }
		this.buttonBarVM.deleteSelectedClickedCallback = { this.listPresenter?.userDeletsSelectedItems() }

		this.deletionViewVM.doneClickedCallback = {
			this.listPresenter?.userClicksDone()
		}

		this.deletionViewVM.selectAllClickedCallback = {
			this.listPresenter?.userClicksSelectAll()
		}

		this.tabView = NavigationDrawerTabLayoutPresenter(
				eventBus = this.eventBus,
				tabLayout = this.tl_navigation_drawer_list,
				onPlaylistSelectedCallback = { this.listPresenter?.currentList = List.Playlist },
				onSoundSheetsSelectedCallback = { this.listPresenter?.currentList = List.SoundSheet },
				onSoundLayoutsSelectedCallback = { this.listPresenter?.currentList = List.SoundLayouts }
		)
	}

	override fun onResume() {
		super.onResume()

		this.headerVM.title = this.soundLayoutManager.soundLayouts.activeLayout.label

		this.tabView?.onAttached()
		this.listPresenter?.onAttached()

		RxNewSoundLayoutManager.soundLayoutsChanges(this.soundLayoutManager)
				.bindToLifecycle(this)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { layouts ->
					val selectedLayout = layouts.activeLayout
					this.headerVM.title = selectedLayout.label
					this.adapterSoundLayouts.notifyDataSetChanged()
				}

		this.adapterSoundLayouts.clicksSettings
				.bindToLifecycle(this)
				.subscribe { viewHolder ->
					viewHolder.data?.let { soundLayout ->
						GenericRenameDialogs.showRenameSoundLayoutDialog(this.fragmentManager, soundLayout) }
				}

		this.adapterSoundLayouts.clicksViewHolder
				.bindToLifecycle(this)
				.subscribe { viewHolder ->
					viewHolder.data?.let { soundLayout ->
						this.listPresenter?.userClicksSoundLayout(soundLayout)
					}
				}

		RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
				.bindToLifecycle(this)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.adapterSoundSheets.notifyDataSetChanged()
				}

		RxSoundManager.changesSoundList(this.soundManager)
				.bindToLifecycle(this)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.adapterSoundSheets.notifyDataSetChanged()
				}

		this.adapterSoundSheets.clicksViewHolder
				.bindToLifecycle(this)
				.subscribe { viewHolder ->
					viewHolder.data?.let { this.listPresenter?.userClicksSoundSheetItem(it) }
				}

		RxNewPlaylistManager.playlistChanges(this.playlistManager)
				.bindToLifecycle(this)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.adapterPlaylist.notifyDataSetChanged()
				}

		this.adapterPlaylist.clicksViewHolder
				.bindToLifecycle(this)
				.subscribe { viewHolder ->
					viewHolder.player?.let{ this.listPresenter?.userClicksPlaylistItem(it) }
				}
	}

	override fun onPause() {
		super.onPause()

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

	fun setActionModeTitle(stringId: Int) {
		this.context?.resources?.let { res ->
			val title = res.getString(stringId)
			this.deletionViewVM.title = title
		}
	}

	fun setActionModeSubTitle(count: Int, maxValue: Int) {
		this.deletionViewVM.maxCount = maxValue
		this.deletionViewVM.selectionCount = count
	}

	fun setDeletionModeUi(enable: Boolean) {

		// disable parallax scrolling if in deletion mode
		this.binding.clNavigationDrawer.isScrollingEnabled = !enable
		this.binding.rvNavigationDrawerList.isNestedScrollingEnabled = !enable

		// hide header and show deletion ui
		this.binding.ablNavigationDrawer.setExpanded(!enable, true)

		// make sure we scroll to valid position after some items may have been deleted
		if (!enable)
			this.binding.rvNavigationDrawerList.scrollToPosition(0)

		// enable deletion actions
		this.deletionViewVM.isEnable = enable
		this.buttonBarVM.enableDeleteSelected = enable
	}
}

