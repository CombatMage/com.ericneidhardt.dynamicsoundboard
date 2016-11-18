package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData

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