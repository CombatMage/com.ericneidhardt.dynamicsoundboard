package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.support.design.widget.AppBarLayout
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletion
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletionListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.createPlaylistPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.createSoundLayoutsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.createSoundSheetPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerButtonBarVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerDeletionViewVM
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
import org.neidhardt.ui_utils.views.NonTouchableCoordinatorLayout
import org.neidhardt.utils.registerIfRequired
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 03.05.2016.
 */
interface NavigationDrawerListLayout {
	var currentList: List

	fun onAttached()

	fun onDetached()
}

enum class List {
	SoundSheet,
	Playlist,
	SoundLayouts
}

class NavigationDrawerListPresenter(
		private val eventBus: EventBus,
		private val coordinatorLayout: NonTouchableCoordinatorLayout,
		private val appBarLayout: AppBarLayout,
		private val recyclerView: RecyclerView,

		private val deletionViewVM: NavigationDrawerDeletionViewVM,
		private val buttonBarVM: NavigationDrawerButtonBarVM,


		//private val buttonCancelActionMode: View,
		//private val buttonSelectAll: View,

		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage,
		private val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,
		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundLayoutsStorage: SoundLayoutsStorage,
		//private val actionModeTitle: TextView,
		//private val actionModeSubTitle: TextView,
		private val fragmentManager: FragmentManager,
		private val soundLayoutsUtil: SoundLayoutsUtil,
		private val soundSheetsDataUtil: SoundSheetsDataUtil
) :
		NavigationDrawerListLayout,
		ItemSelectedForDeletionListener {

	private var currentPresenter: NavigationDrawerListPresenter? = null

	private val presenterSoundSheets = createSoundSheetPresenter(eventBus, recyclerView, soundsDataAccess, soundsDataStorage, soundSheetsDataAccess, soundSheetsDataStorage)
	private val presenterPlaylist = createPlaylistPresenter(eventBus, recyclerView, soundsDataAccess, soundsDataStorage)
	private val presenterSoundLayouts = createSoundLayoutsPresenter(eventBus, recyclerView, soundLayoutsAccess, soundLayoutsStorage)

	private var currentListBacking by Delegates.notNull<List>()
	override var currentList: List
		get() = this.currentListBacking
		set(value) {
			this.currentPresenter?.onDetachedFromWindow()
			val res = this.recyclerView.context.resources
			when (value) {
				List.Playlist -> {
					this.currentListBacking = List.Playlist
					this.currentPresenter = this.presenterPlaylist
					this.recyclerView.adapter = this.presenterPlaylist.adapter
					this.deletionViewVM.title = res.getString(R.string.cab_title_delete_play_list_sounds)
				}
				List.SoundLayouts -> {
					this.currentListBacking = List.SoundLayouts
					this.currentPresenter = this.presenterSoundLayouts
					this.recyclerView.adapter = this.presenterSoundLayouts.adapter
					this.deletionViewVM.title = res.getString(R.string.cab_title_delete_sound_layouts)
				}
				List.SoundSheet -> {
					this.currentListBacking = List.SoundSheet
					this.currentPresenter = this.presenterSoundSheets
					this.recyclerView.adapter = this.presenterSoundSheets.adapter
					this.deletionViewVM.title = res.getString(R.string.cab_title_delete_sound_sheets)
				}
			}
			this.currentPresenter?.onAttachedToWindow()
		}

	init {
		this.buttonBarVM.onAddClicked = { this.add() }
		this.buttonBarVM.onDeleteClicked = { this.prepareDeletion() }
		this.buttonBarVM.onDeleteSelectedClicked = { this.deleteSelected() }

		this.deletionViewVM.onDoneClicked = {
			this.currentPresenter?.stopDeletionMode()
			this.hideToolbarForDeletion()
		}

		this.deletionViewVM.onSelectAllClicked = {
			this.currentPresenter?.selectAllItems()
			val itemCount = this.currentPresenter?.itemCount ?: 0
			this.setActionModeSubTitle(itemCount, itemCount)
		}
	}

	override fun onAttached() {
		this.eventBus.registerIfRequired(this)
		this.currentList = List.SoundSheet
	}

	override fun onDetached() {
		this.eventBus.unregister(this)
	}

	private fun showToolbarForDeletion() {
		this.coordinatorLayout.isScrollingEnabled = false
		this.recyclerView.isNestedScrollingEnabled = false

		val distance = this.recyclerView.width

		this.appBarLayout.setExpanded(false, true)

		this.deletionViewVM.isEnable = true
		/*this.toolbarDeletion.apply {
			this.visibility = View.VISIBLE
			this.translationX = (-distance).toFloat()
			this.animate()
					.withLayer()
					.translationX(0f)
					.setDuration(this.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
					.setInterpolator(DecelerateInterpolator())
					.start()
		}*/

		this.buttonBarVM.enableDeleteSelected = true
	}

	private fun hideToolbarForDeletion() {
		this.coordinatorLayout.isScrollingEnabled = true
		this.recyclerView.isNestedScrollingEnabled = true

		this.appBarLayout.setExpanded(true, true)

		this.recyclerView.scrollToPosition(0)

		this.deletionViewVM.isEnable = false
		this.buttonBarVM.enableDeleteSelected = false
	}

	private fun add() {
		when (this.currentList) {
			List.SoundLayouts -> AddNewSoundLayoutDialog.showInstance(this.fragmentManager, this.soundLayoutsUtil.getSuggestedName())
			List.Playlist -> AddNewSoundDialog(this.fragmentManager, PlaylistTAG)
			List.SoundSheet -> AddNewSoundSheetDialog.showInstance(this.fragmentManager, this.soundSheetsDataUtil.getSuggestedName())
		}
	}

	private fun prepareDeletion() {
		val itemCount = this.currentPresenter?.itemCount ?: 0
		if (itemCount == 0) return
		this.showToolbarForDeletion()
		this.currentPresenter?.startDeletionMode()
		this.setActionModeSubTitle(0, itemCount)
	}

	private fun deleteSelected() {
		this.currentPresenter?.deleteSelectedItems()
		this.currentPresenter?.stopDeletionMode()
		this.hideToolbarForDeletion()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: ItemSelectedForDeletion) {
		this.setActionModeSubTitle(event.selectedItemCount, event.itemCount)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	private fun setActionModeSubTitle(count: Int, maxValue: Int) {
		this.deletionViewVM.maxCount = maxValue
		this.deletionViewVM.selectionCount = count
	}

}