package org.neidhardt.dynamicsoundboard.soundmanagement.events

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 05.07.2015.
 */
public data class SoundAddedEvent(public val player: MediaPlayerController)

public data class SoundChangedEvent(public val player: MediaPlayerController)

public data class SoundMovedEvent(public val player: MediaPlayerController, public val from: Int, public val to: Int)

public data class CreatingPlayerFailedEvent(public val failingPlayerData: MediaPlayerData)

public class PlaylistChangedEvent

public data class SoundsRemovedEvent(public var players: List<MediaPlayerController>?)
{
	public constructor() : this(null)

	public fun removeAll(): Boolean
	{
		return this.players == null
	}
}
