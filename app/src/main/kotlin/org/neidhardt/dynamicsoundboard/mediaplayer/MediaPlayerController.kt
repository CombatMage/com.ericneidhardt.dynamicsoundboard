package org.neidhardt.dynamicsoundboard.mediaplayer

import android.support.annotation.CheckResult
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.ui_utils.views.CustomEditText
import rx.Observable

/**
 * File created by eric.neidhardt on 10.11.2015.
 */

interface MediaPlayerController
{
	var isDeletionPending: Boolean

	val albumCover: ByteArray?

	val mediaPlayerData: MediaPlayerData

	val isPlayingSound: Boolean

	val trackDuration: Int

	var progress: Int

	var volume: Float

	var isLoopingEnabled: Boolean

	var isInPlaylist: Boolean

	fun playSound(): Boolean

	fun stopSound(): Boolean

	fun pauseSound(): Boolean

	fun fadeOutSound()

	fun setSoundUri(uri: String)

	fun destroy(postStateChanged: Boolean)

	var onProgressChangedEventListener: MediaPlayerController.OnProgressChangedEventListener?

	interface OnProgressChangedEventListener {
		fun onProgressChanged(progress: Int)
	}
}

object RxMediaPlayerController {
	@CheckResult
	fun plays(player: MediaPlayerController): Observable<Int> {
		return Observable.create({ subscriber ->
			player.onProgressChangedEventListener = object : MediaPlayerController.OnProgressChangedEventListener {
				override fun onProgressChanged(progress: Int) {
					subscriber.onNext(progress)
				}
			}
		})
	}
}