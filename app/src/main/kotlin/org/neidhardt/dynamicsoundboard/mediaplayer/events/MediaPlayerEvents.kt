package org.neidhardt.dynamicsoundboard.mediaplayer.events

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlayerAction

/**
 * File created by eric.neidhardt on 02.11.2015.
 */
data class MediaPlayerCompletedEvent(val player: MediaPlayerController)

data class MediaPlayerFailedEvent(val player: MediaPlayerController, val failingAction: PlayerAction)

data class MediaPlayerStateChangedEvent(val player: MediaPlayerController, val isAlive: Boolean)
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
	 * @param event delivered MediaPlayerCompletedEvent
	 */
	fun onEvent(event: MediaPlayerCompletedEvent)
}

interface MediaPlayerFailedEventListener
{
	/**
	 * this is called by greenrobot eventbus in case a mediaplayer has thrown an exception.
	 * @param event delivered mediaplayerfailedevent
	 */
	fun onEvent(event: MediaPlayerFailedEvent)

}