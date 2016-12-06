package org.neidhardt.dynamicsoundboard.soundmanagement.model

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.ISoundLayoutManager
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.activeLayout

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
internal val DB_SOUNDS_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds"
internal val DB_SOUNDS_PLAYLIST_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist"

private val DB_SOUNDS = "db_sounds"
private val DB_SOUNDS_PLAYLIST = "db_sounds_playlist"

internal fun getDatabaseNameSounds(manager: ISoundLayoutManager): String
{
	val activeLayout = manager.soundLayouts.activeLayout
	if (activeLayout.isDefaultLayout)
		return DB_SOUNDS_DEFAULT
	val baseName = activeLayout.databaseId
	return baseName + DB_SOUNDS
}

internal fun getDatabaseNamePlayList(manager: ISoundLayoutManager): String
{
	val activeLayout = manager.soundLayouts.activeLayout
	if (activeLayout.isDefaultLayout)
		return DB_SOUNDS_PLAYLIST_DEFAULT
	val baseName = activeLayout.databaseId
	return baseName + DB_SOUNDS_PLAYLIST
}

internal fun searchInListForId(playerId: String, sounds: List<MediaPlayerController>): MediaPlayerController?
{
	return sounds.firstOrNull { player -> player.mediaPlayerData.playerId == playerId }
}

internal fun searchInMapForId(playerId: String, sounds: Map<String, List<MediaPlayerController>>): MediaPlayerController?
{
	val soundSheets = sounds.keys
	return soundSheets
			.map { sounds[it] }
			.map { searchInListForId(playerId, it.orEmpty()) }
			.firstOrNull { it != null }
}

