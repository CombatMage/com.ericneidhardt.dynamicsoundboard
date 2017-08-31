package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dialog.GenericAddDialogs
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.PlaylistPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsPresenter
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsPresenter
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import java.lang.ref.WeakReference

/**
 * @author eric.neidhardt on 03.05.2016.
 */
enum class List {
	SoundSheet,
	Playlist,
	SoundLayouts
}

class NavigationDrawerListPresenter(
		eventBus: EventBus,
		fragment: NavigationDrawerFragment,
		private val recyclerView: RecyclerView
) {

	private val fragmentReference: WeakReference<NavigationDrawerFragment> = WeakReference(fragment)

	var currentPresenter: NavigationDrawerListPresenter? = null

	private val presenterSoundSheets = SoundSheetsPresenter
			.createSoundSheetPresenter(fragment.adapterSoundSheets)
	private val presenterPlaylist = PlaylistPresenter
			.createPlaylistPresenter(eventBus, fragment.adapterPlaylist)
	private val presenterSoundLayouts = SoundLayoutsPresenter
			.createSoundLayoutsPresenter(fragment.adapterSoundLayouts)

	private var currentListBacking: List? = null
	var currentList: List?
		get() = this.currentListBacking
		set(value) {
			if (value == this.currentListBacking) return
			this.currentPresenter?.onDetachedFromWindow()
			when (value) {
				List.Playlist -> {
					this.currentListBacking = List.Playlist
					this.currentPresenter = this.presenterPlaylist
					this.recyclerView.adapter = this.presenterPlaylist.adapter
					this.fragmentReference.get()?.setActionModeTitle(R.string.cab_title_delete_play_list_sounds)
				}
				List.SoundLayouts -> {
					this.currentListBacking = List.SoundLayouts
					this.currentPresenter = this.presenterSoundLayouts
					this.recyclerView.adapter = this.presenterSoundLayouts.adapter
					this.fragmentReference.get()?.setActionModeTitle(R.string.cab_title_delete_sound_layouts)
				}
				List.SoundSheet -> {
					this.currentListBacking = List.SoundSheet
					this.currentPresenter = this.presenterSoundSheets
					this.recyclerView.adapter = this.presenterSoundSheets.adapter
					this.fragmentReference.get()?.setActionModeTitle(R.string.cab_title_delete_sound_sheets)
				}
			}
			this.currentPresenter?.onAttachedToWindow()
		}

	init {

	}

	fun onAttached() {
		this.currentList = List.SoundSheet
	}

	fun onDetached() {
		this.presenterSoundSheets.onDetachedFromWindow()
		this.presenterPlaylist.onDetachedFromWindow()
		this.presenterSoundLayouts.onDetachedFromWindow()
	}

	private fun showToolbarForDeletion() {
		this.fragmentReference.get()?.setDeletionModeUi(true)
	}

	private fun hideToolbarForDeletion() {
		this.fragmentReference.get()?.setDeletionModeUi(false)
	}

	fun userClicksAdd() {
		val fragmentManager = this.fragmentReference.get()?.fragmentManager ?: return
		when (this.currentList) {
			List.SoundLayouts -> GenericAddDialogs.showAddSoundLayoutDialog(fragmentManager)
			List.Playlist -> AddNewSoundDialog.show(fragmentManager, PlaylistTAG)
			List.SoundSheet -> GenericAddDialogs.showAddSoundSheetDialog(fragmentManager)
		}
	}

	fun userStartsDeletionMode() {
		val itemCount = this.currentPresenter?.itemCount ?: 0
		if (itemCount == 0) return
		this.showToolbarForDeletion()
		this.currentPresenter?.startDeletionMode()
		this.fragmentReference.get()?.setActionModeSubTitle(0, itemCount)
	}

	fun userDeletsSelectedItems() {
		this.currentPresenter?.deleteSelectedItems()
		this.hideToolbarForDeletion()
	}

	fun userClicksSelectAll() {
		this.currentPresenter?.selectAllItems()
		val itemCount = this.currentPresenter?.itemCount ?: 0
		this.fragmentReference.get()?.setActionModeSubTitle(itemCount, itemCount)
	}

	fun userClicksDone() {
		this.currentPresenter?.stopDeletionMode()
		this.hideToolbarForDeletion()
	}

	fun userClicksSoundLayout(soundLayout: SoundLayout) {
		this.presenterSoundLayouts.onItemClick(soundLayout)
		if (this.presenterSoundLayouts.isInSelectionMode) {
			val itemCount = this.presenterSoundLayouts.itemCount
			val selectedItemCount = this.presenterSoundLayouts.numberOfItemsSelectedForDeletion
			this.fragmentReference.get()?.setActionModeSubTitle(selectedItemCount, itemCount)
		}
	}

	fun userClicksPlaylistItem(player: MediaPlayerController) {
		this.presenterPlaylist.onItemClick(player)
		if (this.presenterPlaylist.isInSelectionMode) {
			val itemCount = this.presenterPlaylist.itemCount
			val selectedItemCount = this.presenterPlaylist.numberOfItemsSelectedForDeletion
			this.fragmentReference.get()?.setActionModeSubTitle(selectedItemCount, itemCount)
		}
	}

	fun userClicksSoundSheetItem(soundSheet: SoundSheet) {
		this.presenterSoundSheets.onItemClick(soundSheet)
		if (this.presenterSoundSheets.isInSelectionMode) {
			val itemCount = this.presenterSoundSheets.itemCount
			val selectedItemCount = this.presenterSoundSheets.numberOfItemsSelectedForDeletion
			this.fragmentReference.get()?.setActionModeSubTitle(selectedItemCount, itemCount)
		}
	}

}