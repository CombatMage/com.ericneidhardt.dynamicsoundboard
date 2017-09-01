package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
interface NavigationDrawerFragmentContract {
	interface View {
		fun showSoundSheets()
		fun showPlaylist()
		fun showSoundLayouts()
		fun showDialogAddSoundSheet()
		fun showDialogAddSoundToPlaylist()
		fun showDialogAddSoundLayout()
		fun closeNavigationDrawer()
		fun showDeletionModeSoundSheets()
		fun showDeletionModePlaylist()
		fun showDeletionModeSoundLayouts()
	}
	interface Presenter {
		fun viewCreated()
		fun userClicksTabSoundSheets()
		fun userClicksTabPlaylist()
		fun userClicksHeaderSoundLayout()
		fun userClicksAdd()
		fun userClicksSelectAll()
		fun userClicksDelete()
		fun userClicksDeleteSelected()
		fun userClicksSoundSheet(soundSheet: SoundSheet)
		fun userClicksPlaylistSound(player: MediaPlayerController)
		fun userClicksSoundLayoutItem(soundLayout: SoundLayout)
	}
	interface Model {

	}
}