package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet


/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
class NavigationDrawerFragmentPresenter(
		private val view: NavigationDrawerFragmentContract.View,
		private val model: NavigationDrawerFragmentContract.Model
) : NavigationDrawerFragmentContract.Presenter {

	private val INDEX_NOT_SET = -1

	private enum class List {
		SoundSheet,
		Playlist,
		SoundLayouts
	}

	private var currentList: List = List.SoundSheet

	override fun viewCreated() {
		this.model.soundSheets
				.subscribe { soundSheets -> this.view.displayedSoundSheets = soundSheets }

		this.model.playList
				.subscribe { playList -> this.view.displayedPlaylist = playList }

		this.model.mediaPlayerStateChangedEvents
				.filter { (player) -> player.mediaPlayerData.fragmentTag == PlaylistTAG }
				.subscribe { (player, isAlive) -> this.onPlayListPlayerStateChanged(player, isAlive) }

		this.model.mediaPlayerCompletedEvents
				.map { (player) -> player }
				.filter { player -> player.mediaPlayerData.fragmentTag == PlaylistTAG }
				.subscribe { player -> this.onPlayListPlayerCompleted(player) }

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
			this.view.showSoundLayouts()
			this.view.animateHeaderArrow(NavigationDrawerFragmentContract.View.AnimationDirection.UP)
		} else { // hide sound sound layouts
			this.currentList = List.SoundSheet
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
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksDelete() {
		when(this.currentList) {
			NavigationDrawerFragmentPresenter.List.SoundSheet -> this.view.showDeletionModeSoundSheets()
			NavigationDrawerFragmentPresenter.List.Playlist -> this.view.showDeletionModePlaylist()
			NavigationDrawerFragmentPresenter.List.SoundLayouts -> this.view.showDialogAddSoundLayout()
		}
	}

	override fun userClicksDeleteCancel() {
		this.view.stopDeletionMode()
	}

	override fun userClicksDeleteSelected() {
		this.view.stopDeletionMode()
	}

	override fun userClicksSoundSheet(soundSheet: SoundSheet) {
		this.model.setSoundSheetSelected(soundSheet)
	}

	override fun userClicksSoundLayoutItem(soundLayout: SoundLayout) {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksSoundLayoutSettings(soundLayout: SoundLayout) {
		this.view.showDialogRenameSoundLayout(soundLayout)
	}

	/*
	 * playlist handling
	 */
	private var currentPlayListItemIndex: Int = INDEX_NOT_SET

	override fun userClicksPlaylistSound(player: MediaPlayerController) {
		this.startOrStopPlayList(player)
	}

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

		if (nextActivePlayer.isPlayingSound)
			nextActivePlayer.stopSound()
		else
			nextActivePlayer.playSound()

		this.view.displayedPlaylist = currentPlayList // set playlist to display
	}

	private fun onPlayListPlayerStateChanged(player: MediaPlayerController, isPlayerRemoved: Boolean) {
		/*val currentPlayList = this.view.displayedPlaylist // get current playlist

		// TODO handle this in model
		// TODO check if relevant
		if (currentPlayList.contains(player) && isPlayerRemoved) {
		}

		this.view.displayedPlaylist = currentPlayList // set playlist to display

		if (this.values.contains(player) && !event.isAlive) { // removed a destroyed media player
			val index = this.values.indexOf(player)
			this.manager.remove(listOf(player))
			this.adapter.notifyItemRemoved(index)
		}
		else
			this.adapter.notifyDataSetChanged() */
	}

	private fun onPlayListPlayerCompleted(player: MediaPlayerController) {
		val currentPlayList = this.view.displayedPlaylist // get current playlist

		val currentPlayer = currentPlayList[this.currentPlayListItemIndex]
		currentPlayer.stopSound()

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