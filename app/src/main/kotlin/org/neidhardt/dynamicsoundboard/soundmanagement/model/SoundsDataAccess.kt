package org.neidhardt.dynamicsoundboard.soundmanagement.model

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public interface SoundsDataAccess
{
	/**
	 * Retrieve a set of currently playing sound in the whole SoundBoard.
	 * @return a set of currently playing sounds
	 */
	public fun getCurrentlyPlayingSounds(): Set<EnhancedMediaPlayer>

	/**
	 * Retrieve a list of sounds currently in Playlist
	 * @return a list of sound in the current Playlist
	 */
	public fun getPlaylist(): List<EnhancedMediaPlayer>

	/**
	 * Retrieve a map of all sound in every SoundSheet.
	 * @return a map of sounds, currently added sound to SoundSheets
	 */
	public fun getSounds(): Map<String, List<EnhancedMediaPlayer>>

	/**
	 * Retrieve a List of sound in a certain SoundSheet. The SoundSheet is identified by it's fragment tag.
	 * @param fragmentTag TAG of SoundSheet
	 * *
	 * @return a List of sounds in the requested SoundSheet
	 */
	public fun getSoundsInFragment(fragmentTag: String): List<EnhancedMediaPlayer>

	/**
	 * Retrieve a certain sound from the list of the sound in the given fragment.
	 * @param fragmentTag TAG of SoundSheet
	 * *
	 * @param playerId id of the player
	 * *
	 * @return EnhancedMediaPlayer if the player was found, else null
	 */
	public fun getSoundById(fragmentTag: String, playerId: String): EnhancedMediaPlayer
}
