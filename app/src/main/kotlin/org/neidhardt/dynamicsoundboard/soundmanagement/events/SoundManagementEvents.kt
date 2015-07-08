package org.neidhardt.dynamicsoundboard.soundmanagement.events

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer

/**
 * File created by eric.neidhardt on 05.07.2015.
 */
data
public class SoundAddedEvent(public val player: EnhancedMediaPlayer)

data
public class SoundChangedEvent(public val player: EnhancedMediaPlayer)

data
public class SoundMovedEvent(public val player: EnhancedMediaPlayer, public val from: Int, public val to: Int)

data
public class CreatingPlayerFailedEvent(public val failingPlayerData: MediaPlayerData)

data
public class PlaylistChangedEvent

data
public class SoundsRemovedEvent(
		public var players: List<EnhancedMediaPlayer>?
)
{
	public constructor() : this(null)

	public fun removeAll(): Boolean
	{
		return this.players == null
	}
}
