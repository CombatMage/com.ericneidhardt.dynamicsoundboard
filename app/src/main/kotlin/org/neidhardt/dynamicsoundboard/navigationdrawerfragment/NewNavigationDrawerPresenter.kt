package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
class NewNavigationDrawerPresenter(
		private val view: NavigationDrawerFragmentContract.View
) : NavigationDrawerFragmentContract.Presenter {

	private enum class List {
		SoundSheet,
		Playlist,
		SoundLayouts
	}

	private var currentList: List = List.SoundSheet

	override fun viewCreated() {
		this.view.stopDeletionMode()

		this.view.showSoundSheets()

		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksTabSoundSheets() {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksTabPlaylist() {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksHeaderSoundLayout() {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksAdd() {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksSelectAll() {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun userClicksDelete() {
		when(this.currentList) {
			NewNavigationDrawerPresenter.List.SoundSheet -> this.view.showDeletionModeSoundSheets()
			NewNavigationDrawerPresenter.List.Playlist -> this.view.showDeletionModePlaylist()
			NewNavigationDrawerPresenter.List.SoundLayouts -> this.view.showDialogAddSoundLayout()
		}
	}

	override fun userClicksDeleteCancel() {
		this.view.stopDeletionMode()
	}

	override fun userClicksDeleteSelected() {
		this.view.stopDeletionMode()
	}

	override fun userClicksSoundSheet(soundSheet: SoundSheet) {
		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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