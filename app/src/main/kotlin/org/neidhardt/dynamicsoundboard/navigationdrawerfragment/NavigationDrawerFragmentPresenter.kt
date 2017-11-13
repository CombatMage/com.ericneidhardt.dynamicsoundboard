package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import android.util.Log
import org.neidhardt.dynamicsoundboard.manager.selectedLayout
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet


/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
private const val INDEX_NOT_SET = -1

class NavigationDrawerFragmentPresenter(
		private val view: NavigationDrawerFragmentContract.View,
		private val model: NavigationDrawerFragmentContract.Model
) : NavigationDrawerFragmentContract.Presenter {

	private val logTag = javaClass.name

	private enum class List {
		SoundSheet,
		Playlist,
		SoundLayouts
	}

	private var isSelectionModeActive = false
	private var currentList: List = List.SoundSheet

	override fun viewCreated() {
		this.model.soundSheets
				.subscribe { soundSheets -> this.view.displayedSoundSheets = soundSheets }

		this.model.playList
				.subscribe { playList -> this.view.displayedPlaylist = playList }

		this.model.soundLayouts
				.subscribe { soundLayouts ->
					this.view.setHeaderTitle(soundLayouts.selectedLayout?.label ?: "")
					this.view.displayedSoundLayouts = soundLayouts
				}

		this.model.mediaPlayerStateChangedEvents
				.filter { (player) -> player.mediaPlayerData.fragmentTag == PlaylistTAG }
				.subscribe { (player, isAlive) -> this.onPlayListPlayerStateChanged(player, isAlive) }

		this.model.mediaPlayerCompletedEvents
				.map { (player) -> player }
				.filter { player -> player.mediaPlayerData.fragmentTag == PlaylistTAG }
				.subscribe { player -> this.onPlayListPlayerCompleted(player) }

		this.view.setTapBarState(NavigationDrawerFragmentContract.View.TapBarState.NORMAL)
		this.view.stopDeletionMode()
		this.view.showSoundSheets()
	}

	override fun userClicksTabSoundSheets() {
		this.currentList = List.SoundSheet
		this.view.showSoundSheets()
	}

	override fun userClicksTabPlaylist() {
		this.currentList = List.Playlist
		this.view.showPlaylist()
	}

	override fun userClicksHeaderSoundLayout() {
		if (this.currentList != List.SoundLayouts) { // show sound layouts
			this.currentList = List.SoundLayouts
			this.view.setTapBarState(NavigationDrawerFragmentContract.View.TapBarState.SOUND_LAYOUTS)
			this.view.showSoundLayouts()
			this.view.animateHeaderArrow(NavigationDrawerFragmentContract.View.AnimationDirection.UP)
		} else { // hide sound sound layouts
			this.currentList = List.SoundSheet
			this.view.setTapBarState(NavigationDrawerFragmentContract.View.TapBarState.NORMAL)
			this.view.showSoundSheets()
			this.view.animateHeaderArrow(NavigationDrawerFragmentContract.View.AnimationDirection.DOWN)
		}
	}

	override fun userClicksAdd() {
		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> this.view.showDialogAddSoundSheet()
			NavigationDrawerFragmentPresenter.List.Playlist -> this.view.showDialogAddSoundToPlaylist()
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> this.view.showDialogAddSoundLayout()
		}
	}

	override fun userClicksSelectAll() {
		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> {
				val items = this.view.displayedSoundSheets
				items.forEach { it.isSelectedForDeletion = true }
				this.view.displayedSoundSheets = items
				this.view.setSelectedItemCount(items.size, items.size)
			}
			NavigationDrawerFragmentPresenter.List.Playlist -> {
				val items = this.view.displayedPlaylist
				items.forEach { it.mediaPlayerData.isSelectedForDeletion = true }
				this.view.displayedPlaylist = items
				this.view.setSelectedItemCount(items.size, items.size)
			}
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> {
				val items = this.view.displayedSoundLayouts
				items.forEach { it.isSelectedForDeletion = true }
				this.view.displayedSoundLayouts = items
				this.view.setSelectedItemCount(items.size, items.size)
			}
		}
	}

	override fun userClicksDelete() {
		this.isSelectionModeActive = true

		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> {
				this.view.setSelectedItemCount(0, this.view.displayedSoundSheets.size)
				this.view.showDeletionModeSoundSheets()
			}
			NavigationDrawerFragmentPresenter.List.Playlist -> {
				this.view.setSelectedItemCount(0, this.view.displayedPlaylist.size)
				this.view.showDeletionModePlaylist()
			}
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> {
				this.view.setSelectedItemCount(0, this.view.displayedSoundLayouts.size)
				this.view.showDeletionModeSoundLayouts()
			}
		}
	}

	override fun userClicksDeleteCancel() {
		this.isSelectionModeActive = false
		this.view.stopDeletionMode()

		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> {
				val items = this.view.displayedSoundSheets
				items.forEach { it.isSelectedForDeletion = false }
				this.view.displayedSoundSheets = items
				this.view.setSelectedItemCount(0, items.size)
			}
			NavigationDrawerFragmentPresenter.List.Playlist -> {
				val items = this.view.displayedPlaylist
				items.forEach { it.mediaPlayerData.isSelectedForDeletion = false }
				this.view.displayedPlaylist = items
				this.view.setSelectedItemCount(0, items.size)
			}
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> {
				val items = this.view.displayedSoundLayouts
				items.forEach { it.isSelectedForDeletion = false }
				this.view.displayedSoundLayouts = items
				this.view.setSelectedItemCount(0, items.size)
			}
		}
	}

	override fun userClicksDeleteSelected() {
		this.isSelectionModeActive = false
		this.view.stopDeletionMode()

		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> {
				val itemsToDelete = this.view.displayedSoundSheets.filter {
					it.isSelectedForDeletion
				}
				this.model.deleteSoundSheets(itemsToDelete)
			}
			NavigationDrawerFragmentPresenter.List.Playlist -> {
				val itemsToDelete = this.view.displayedPlaylist.filter {
					it.mediaPlayerData.isSelectedForDeletion
				}
				this.model.deletePlayListPlayer(itemsToDelete)
			}
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> {
				val itemsToDelete = this.view.displayedSoundLayouts.filter {
					it.isSelectedForDeletion
				}
				this.model.deleteSoundLayouts(itemsToDelete)
			}
		}
	}

	override fun userClicksSoundSheet(soundSheet: SoundSheet) {
		if (this.isSelectionModeActive) {
			soundSheet.isSelectedForDeletion = !soundSheet.isSelectedForDeletion

			val items = this.view.displayedSoundSheets
			val selectedCount = items.filter { it.isSelectedForDeletion }.size

			this.view.setSelectedItemCount(selectedCount, items.size)
			this.view.displayedSoundSheets = items
		} else {
			this.view.closeNavigationDrawer()
			this.model.setSoundSheetSelected(soundSheet)
		}
	}

	override fun userClicksSoundLayoutItem(soundLayout: SoundLayout) {
		if (this.isSelectionModeActive) {
			soundLayout.isSelectedForDeletion = !soundLayout.isSelectedForDeletion

			val items = this.view.displayedSoundLayouts
			val selectedCount = items.filter { it.isSelectedForDeletion }.size

			this.view.setSelectedItemCount(selectedCount, items.size)
			this.view.displayedSoundLayouts = items
		} else {
			this.currentList = List.SoundSheet
			this.view.setTapBarState(NavigationDrawerFragmentContract.View.TapBarState.NORMAL)
			this.view.showSoundSheets()
			this.view.animateHeaderArrow(NavigationDrawerFragmentContract.View.AnimationDirection.DOWN)

			this.view.setHeaderTitle(soundLayout.label)
			this.view.closeNavigationDrawer()
			this.model.setSoundLayoutSelected(soundLayout)
		}
	}

	override fun userClicksPlaylistSound(player: MediaPlayerController) {
		if (this.isSelectionModeActive) {
			player.mediaPlayerData.isSelectedForDeletion = !player.mediaPlayerData.isSelectedForDeletion

			val items = this.view.displayedPlaylist
			val selectedCount = items.filter { it.mediaPlayerData.isSelectedForDeletion }.size

			this.view.setSelectedItemCount(selectedCount, items.size)
			this.view.displayedPlaylist = items
		} else {
			this.startOrStopPlayList(player)
		}
	}

	override fun userClicksSoundLayoutSettings(soundLayout: SoundLayout) {
		this.view.showDialogRenameSoundLayout(soundLayout)
	}

	/*
	 * playlist handling
	 */
	private var currentPlayListItemIndex: Int = INDEX_NOT_SET

	private fun startOrStopPlayList(nextActivePlayer: MediaPlayerController) {
		val currentPlayList = this.view.displayedPlaylist // get current playlist

		if (!currentPlayList.contains(nextActivePlayer))
			throw IllegalStateException("next active player $nextActivePlayer is not in playlist")

		// handle next player to start
		// stop all playing sounds, except the next player
		this.currentPlayListItemIndex = currentPlayList.indexOf(nextActivePlayer)
		currentPlayList
				.filter { it != nextActivePlayer && it.isPlayingSound }
				.forEach { it.stopSound() }

		if (nextActivePlayer.isPlayingSound) {
			nextActivePlayer.stopSound()
		} else {
			nextActivePlayer.playSound()
		}

		this.view.displayedPlaylist = currentPlayList // set playlist to display
	}

	private fun onPlayListPlayerStateChanged(player: MediaPlayerController, isPlayerRemoved: Boolean) {
		Log.d(this.logTag, "onPlayListPlayerStateChanged($player, $isPlayerRemoved)")
		// TODO should update UI
	}

	private fun onPlayListPlayerCompleted(player: MediaPlayerController) {
		val currentPlayList = this.view.displayedPlaylist // get current playlist

		player.stopSound()

		if (this.currentPlayListItemIndex != INDEX_NOT_SET) {
			this.currentPlayListItemIndex += 1
			if (currentPlayList.isEmpty()) {
				this.currentPlayListItemIndex = INDEX_NOT_SET
				return
			}

			if (this.currentPlayListItemIndex >= currentPlayList.size) {
				this.currentPlayListItemIndex = 0
			}

			currentPlayList[this.currentPlayListItemIndex].playSound()
		}

		this.view.displayedPlaylist = currentPlayList // set playlist to display
	}
}