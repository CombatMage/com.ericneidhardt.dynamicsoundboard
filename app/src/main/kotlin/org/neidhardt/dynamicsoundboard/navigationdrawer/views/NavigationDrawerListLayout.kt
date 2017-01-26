package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.views.NonTouchableCoordinatorLayout
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dialog.GenericAddDialogs
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletion
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ItemSelectedForDeletionListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.PlaylistPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerButtonBarVM
import org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel.NavigationDrawerDeletionViewVM
import org.neidhardt.eventbus_utils.registerIfRequired
import java.lang.ref.WeakReference

/**
 * @author eric.neidhardt on 03.05.2016.
 */
interface NavigationDrawerListLayout {
	var currentList: List?

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
		fragment: NavigationDrawerFragment,

		private val coordinatorLayout: NonTouchableCoordinatorLayout,
		private val appBarLayout: AppBarLayout,
		private val recyclerView: RecyclerView,
		private val deletionViewVM: NavigationDrawerDeletionViewVM,
		private val buttonBarVM: NavigationDrawerButtonBarVM
) :
		NavigationDrawerListLayout,
		ItemSelectedForDeletionListener {

	private val fragmentReference: WeakReference<NavigationDrawerFragment> = WeakReference(fragment)

	private var currentPresenter: NavigationDrawerListPresenter? = null

	private val presenterSoundSheets = SoundSheetsPresenter
			.createSoundSheetPresenter(eventBus, fragment.adapterSoundSheets)
	private val presenterPlaylist = PlaylistPresenter
			.createPlaylistPresenter(eventBus, fragment.adapterPlaylist)
	private val presenterSoundLayouts = SoundLayoutsPresenter
			.createSoundLayoutsPresenter(eventBus, fragment.adapterSoundLayouts)

	private var currentListBacking: List? = null
	override var currentList: List?
		get() = this.currentListBacking
		set(value) {
			if (value == this.currentListBacking) return
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
		this.buttonBarVM.addClickedCallback = { this.add() }
		this.buttonBarVM.deleteClickedCallback = { this.prepareDeletion() }
		this.buttonBarVM.deleteSelectedClickedCallback = { this.deleteSelected() }

		this.deletionViewVM.doneClickedCallback = {
			this.currentPresenter?.stopDeletionMode()
			this.hideToolbarForDeletion()
		}

		this.deletionViewVM.selectAllClickedCallback = {
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
		this.presenterSoundSheets.onDetachedFromWindow()
		this.presenterPlaylist.onDetachedFromWindow()
		this.presenterSoundLayouts.onDetachedFromWindow()
	}

	private fun showToolbarForDeletion() {
		this.coordinatorLayout.isScrollingEnabled = false
		this.recyclerView.isNestedScrollingEnabled = false

		this.appBarLayout.setExpanded(false, true)

		this.deletionViewVM.isEnable = true

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
		val fragmentManager = this.fragmentReference.get().fragmentManager ?: return
		when (this.currentList) {
			List.SoundLayouts -> GenericAddDialogs.showAddSoundLayoutDialog(fragmentManager)
			List.Playlist -> AddNewSoundDialog.show(fragmentManager, PlaylistTAG)
			List.SoundSheet -> GenericAddDialogs.showAddSoundSheetDialog(fragmentManager)
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