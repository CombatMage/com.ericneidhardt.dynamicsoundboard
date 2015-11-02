package org.neidhardt.dynamicsoundboard.mediaplayer.events

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer

/**
 * File created by eric.neidhardt on 02.11.2015.
 */
data class MediaPlayerCompletedEvent(val player: EnhancedMediaPlayer)

data class MediaPlayerStateChangedEvent(val player: EnhancedMediaPlayer, val isAlive: Boolean)
{
	val playerId: String
		get() = this.player.mediaPlayerData.playerId

	val fragmentTag: String
		get() = this.player.mediaPlayerData.fragmentTag
}

interface MediaPlayerEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a MediaPlayer changed his state (ie. start or stops playing).
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	fun onEvent(event: MediaPlayerStateChangedEvent)

	/**
	 * This is called by greenRobot EventBus in case a MediaPlayer has finished playing.
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	fun onEvent(event: MediaPlayerCompletedEvent)
}
