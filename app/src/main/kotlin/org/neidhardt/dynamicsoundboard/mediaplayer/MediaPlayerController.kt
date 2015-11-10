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

	var isLoopingEnabled: Boolean

	fun setIsInPlaylist(inPlaylist: Boolean) // TODO convert to property

	fun playSound(): Boolean

	fun stopSound(): Boolean

	fun pauseSound(): Boolean

	fun fadeOutSound()

	fun setSoundUri(uri: String)

	fun destroy(postStateChanged: Boolean)

}