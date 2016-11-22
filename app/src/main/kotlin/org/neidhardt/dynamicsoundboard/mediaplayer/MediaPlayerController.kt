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

	var mOnProgressChangedEventListener: MediaPlayerController.OnProgressChangedEventListener?

	interface OnProgressChangedEventListener {
		fun onProgressChanged(progress: Int, trackDuration: Int)
	}

	fun setOnProgressChangedEventListener(listener: (progress: Int, trackDuration: Int) -> Unit) {
		this.mOnProgressChangedEventListener = object : OnProgressChangedEventListener {
			override fun onProgressChanged(progress: Int, trackDuration: Int) {
				listener.invoke(progress, trackDuration)
			}
		}
	}
}
