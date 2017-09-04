package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import io.reactivex.Observable
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
interface NavigationDrawerFragmentContract {
	interface View {
		enum class AnimationDirection {
			UP,
			DOWN
		}

		var displayedSoundSheets: List<SoundSheet>
		var displayedPlaylist: List<MediaPlayerController>
		var displayedSoundLayouts: List<SoundLayout>

		fun setHeaderTitle(text: String)
		fun animateHeaderArrow(direction: AnimationDirection)
		fun showSoundSheets()
		fun showPlaylist()
		fun showSoundLayouts()
		fun showDialogAddSoundSheet()
		fun showDialogAddSoundToPlaylist()
		fun showDialogAddSoundLayout()
		fun showDialogRenameSoundLayout(soundLayout: SoundLayout)
		fun closeNavigationDrawer()
		fun showDeletionModeSoundSheets()
		fun showDeletionModePlaylist()
		fun showDeletionModeSoundLayouts()
		fun setSelectedItemCount(selectedCount: Int, maxCount: Int)
		fun stopDeletionMode()
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
		fun userClicksDeleteCancel()
		fun userClicksSoundSheet(soundSheet: SoundSheet)
		fun userClicksPlaylistSound(player: MediaPlayerController)
		fun userClicksSoundLayoutItem(soundLayout: SoundLayout)
		fun userClicksSoundLayoutSettings(soundLayout: SoundLayout)
	}
	interface Model {
		val soundSheets: Observable<List<SoundSheet>>
		val playList: Observable<List<MediaPlayerController>>
		val soundLayouts: Observable<List<SoundLayout>>
		val mediaPlayerStateChangedEvents: Observable<MediaPlayerStateChangedEvent>
		val mediaPlayerCompletedEvents: Observable<MediaPlayerCompletedEvent>
		fun deleteSoundSheets(soundSheets: List<SoundSheet>)
		fun deletePlayListPlayer(player: List<MediaPlayerController>)
		fun deleteSoundLayouts(soundLayouts: List<SoundLayout>)
		fun setSoundSheetSelected(soundSheet: SoundSheet)
		fun setSoundLayoutSelected(soundLayout: SoundLayout)
	}
}