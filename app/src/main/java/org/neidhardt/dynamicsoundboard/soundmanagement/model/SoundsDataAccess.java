package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public interface SoundsDataAccess
{
	/**
	 * Retrieve a set of currently playing sound in the whole SoundBoard.
	 * @return a set of currently playing sounds
	 */
	Set<EnhancedMediaPlayer> getCurrentlyPlayingSounds();

	/**
	 * Retrieve a list of sounds currently in Playlist
	 * @return a list of sound in the current Playlist
	 */
	List<EnhancedMediaPlayer> getPlayList();

	/**
	 * Retrieve a map of all sound in every SoundSheet.
	 * @return a map of sounds, currently added sound to SoundSheets
	 */
	Map<String, List<EnhancedMediaPlayer>> getSounds();

	/**
	 * Retrieve a List of sound in a certain SoundSheet. The SoundSheet is identified by it's fragment tag.
	 * @param fragmentTag TAG of SoundSheet
	 * @return a List of sounds in the requested SoundSheet
	 */
	List<EnhancedMediaPlayer> getSoundsInFragment(String fragmentTag);

	/**
	 * Retrieve a certain sound from the list of the sound in the given fragment.
	 * @param fragmentTag TAG of SoundSheet
	 * @param playerId id of the player
	 * @return EnhancedMediaPlayer if the player was found, else null
	 */
	EnhancedMediaPlayer getSoundById(String fragmentTag, String playerId);
}
