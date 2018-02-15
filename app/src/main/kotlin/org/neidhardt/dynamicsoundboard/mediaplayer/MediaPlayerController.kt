package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.dynamicsoundboard.model.MediaPlayerData

/**
 * File created by eric.neidhardt on 10.11.2015.
 */

interface MediaPlayerController
{
	var isDeletionPending: Boolean

	val albumCover: ByteArray?

	val mediaPlayerData: MediaPlayerData

	val isPlayingSound: Boolean

	val isFadingOut: Boolean

	val trackDuration: Int

	var progress: Int

	var volume: Float

	var isLoopingEnabled: Boolean

	fun playSound(): Boolean

	fun stopSound(): Boolean

	fun pauseSound(): Boolean

	fun fadeOutSound()

	fun setSoundUri(uri: String)

	fun destroy(postStateChanged: Boolean)

	var mOnProgressChangedEventListener: MediaPlayerController.OnProgressChangedEventListener?

	interface OnProgressChangedEventListener {
		fun onProgressChanged(player: MediaPlayerController, progress: Int, trackDuration: Int)
	}

	fun setOnProgressChangedEventListener(listener: (player: MediaPlayerController, progress: Int, trackDuration: Int) -> Unit) {
		this.mOnProgressChangedEventListener = object : OnProgressChangedEventListener {
			override fun onProgressChanged(player: MediaPlayerController, progress: Int, trackDuration: Int) {
				listener.invoke(player, progress, trackDuration)
			}
		}
	}
}
