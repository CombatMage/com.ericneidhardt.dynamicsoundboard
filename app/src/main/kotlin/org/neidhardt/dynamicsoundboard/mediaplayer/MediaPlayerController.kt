package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData

/**
 * File created by eric.neidhardt on 10.11.2015.
 */
interface MediaPlayerController
{
	val mediaPlayerData: MediaPlayerData

	val isPlaying: Boolean

	var trackDuration: Int

	var progress: Int

	fun playSound(): Boolean

	fun stopSound(): Boolean

	fun pauseSound(): Boolean

	fun fadeOutSound()

	fun setIsInPlaylist(inPlaylist: Boolean)

	fun setLooping(looping: Boolean)

	fun setSoundUri(uri: String)

	fun destroy(postStateChanged: Boolean)
}