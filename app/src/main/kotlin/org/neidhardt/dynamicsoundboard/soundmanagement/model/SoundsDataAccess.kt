package org.neidhardt.dynamicsoundboard.soundmanagement.model

import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
interface SoundsDataAccess
{
	/**
	 * Retrieve a set of currently playing sound in the whole SoundBoard.
	 * @return a set of currently playing sounds
	 */
	val currentlyPlayingSounds: Set<MediaPlayerController>

	/**
	 * Retrieve a list of sounds currently in Playlist
	 * @return a list of sound in the current Playlist
	 */
	val playlist: List<MediaPlayerController>

	/**
	 * Retrieve a map of all sound in every SoundSheet.
	 * @return a map of sounds, currently added sound to SoundSheets
	 */
	val sounds: Map<String, List<MediaPlayerController>>

	/**
	 * Retrieve a List of sound in a certain SoundSheet. The SoundSheet is identified by it's fragment tag.
	 * @param fragmentTag TAG of SoundSheet
	 * *
	 * @return a List of sounds in the requested SoundSheet
	 */
	fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController>

	/**
	 * Retrieve a certain sound from the list of the sound in the given fragment.
	 * @param fragmentTag TAG of SoundSheet
	 * *
	 * @param playerId id of the player
	 * *
	 * @return EnhancedMediaPlayer if the player was found, else null
	 */
	fun getSoundById(fragmentTag: String, playerId: String): MediaPlayerController?
}
