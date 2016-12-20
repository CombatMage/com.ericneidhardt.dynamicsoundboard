package org.neidhardt.dynamicsoundboard.soundmanagement.events

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData

/**
 * File created by eric.neidhardt on 05.07.2015.
 */
data class SoundAddedEvent(val player: MediaPlayerController)

data class SoundChangedEvent(val player: MediaPlayerController)

data class SoundMovedEvent(val player: MediaPlayerController, val from: Int, val to: Int)

data class CreatingPlayerFailedEvent(val failingPlayerData: NewMediaPlayerData)

class PlaylistChangedEvent

data class SoundsRemovedEvent(var players: List<MediaPlayerController>?)
{
	constructor() : this(null)

	fun removeAll(): Boolean = this.players == null
}
