package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public interface SoundDataModel
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
	 * Add or remove the sound from playlist.
	 * @param playerId id of player to toggle
	 * @param addToPlayList player's state in playlist
	 */
	void toggleSoundInPlaylist(String playerId, boolean addToPlayList);
}
