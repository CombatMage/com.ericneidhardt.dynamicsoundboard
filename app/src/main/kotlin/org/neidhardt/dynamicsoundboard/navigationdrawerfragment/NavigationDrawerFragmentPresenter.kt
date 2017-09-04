package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
class NavigationDrawerFragmentPresenter(
		private val view: NavigationDrawerFragmentContract.View,
		private val model: NavigationDrawerFragmentContract.Model
) : NavigationDrawerFragmentContract.Presenter {

	private enum class List {
		SoundSheet,
		Playlist,
		SoundLayouts
	}

	private var currentList: List = List.SoundSheet

	override fun viewCreated() {
		this.model.soundSheets
				.subscribe { soundSheets -> this.view.displayedSoundSheets = soundSheets }

		this.view.stopDeletionMode()
		this.view.showSoundSheets()

		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

	override fun userClicksPlaylistSound(player: MediaPlayerController) {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksSoundLayoutItem(soundLayout: SoundLayout) {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksSoundLayoutSettings(soundLayout: SoundLayout) {
		this.view.showDialogRenameSoundLayout(soundLayout)
	}
}