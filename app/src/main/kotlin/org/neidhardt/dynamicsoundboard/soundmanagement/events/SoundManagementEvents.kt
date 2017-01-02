package org.neidhardt.dynamicsoundboard.soundmanagement.events

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData

/**
 * File created by eric.neidhardt on 05.07.2015.
 */
data class SoundChangedEvent(val player: MediaPlayerController)

data class CreatingPlayerFailedEvent(val failingPlayerData: NewMediaPlayerData)

class PlaylistChangedEvent

