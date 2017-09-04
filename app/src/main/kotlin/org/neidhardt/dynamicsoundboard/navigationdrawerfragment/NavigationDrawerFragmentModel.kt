package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.manager.PlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerEventListener
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.model.SoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 04.09.2017.
 */
class NavigationDrawerFragmentModel(
		private val soundSheetManager: SoundSheetManager,
		private val playlistManager: PlaylistManager
) :
		NavigationDrawerFragmentContract.Model
{

	override val soundSheets: Observable<List<SoundSheet>>
		get() {
			return RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val playList: Observable<List<MediaPlayerController>>
		get() {
			return RxNewPlaylistManager.playlistChanges(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val mediaPlayerCompletedEvents: Observable<MediaPlayerCompletedEvent>
		get() {
			return RxNewPlaylistManager.playlistPlayerCompletes(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override val mediaPlayerStateChangedEvents: Observable<MediaPlayerStateChangedEvent>
		get() {
			return RxNewPlaylistManager.playlistPlayerStateChanges(this.playlistManager)
					.observeOn(AndroidSchedulers.mainThread())
		}

	override fun setSoundSheetSelected(soundSheet: SoundSheet) {
		this.soundSheetManager.setSelected(soundSheet)
	}
}