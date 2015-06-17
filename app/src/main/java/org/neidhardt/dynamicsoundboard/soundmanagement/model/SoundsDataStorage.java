package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.List;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public interface SoundsDataStorage
{
	void createSoundAndAddToManager(MediaPlayerData data);

	void createPlaylistSoundAndAddToManager(MediaPlayerData data);

	/**
	 * Create an new {@code EnhancedMediaPlayer} from given data and adds this player to the playlist.
	 * If creation failed, a new {@code CreatingPlayerFailedEvent} is posted.
	 * @param player {@code EnhancedMediaPlayer} to add
	 */
	void addSoundToPlayList(EnhancedMediaPlayer player);

	/**
	 * Adds sound to corresponding sound list. If the list is long enough, the players sort order is respected, otherwise it is added to the end of the list.
	 * @param player the new player to add
	 */
	void addSoundToSounds(EnhancedMediaPlayer player);

	/**
	 * Add or remove the sound from playlist.
	 * @param playerId id of player to toggle
	 * @param addToPlaylist player's state in playlist
	 */
	void toggleSoundInPlaylist(String playerId, boolean addToPlaylist);

	/**
	 * Remove all given Sounds from the currently loaded sounds, but not from the playlist.
	 * @param soundsToRemove list of sounds to remove
	 */
	void removeSounds(List<EnhancedMediaPlayer> soundsToRemove);

	/**
	 * Remove all given Sounds from the currently loaded sounds, from the playlist.
	 * @param soundsToRemove list of sounds to remove
	 */
	void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove);

	/**
	 * Moves a certain sound in the list of sounds to another position
	 * @param fragmentTag TAG of SoundSheet
	 * @param from position of moved sound
	 * @param to new position of moved sound
	 */
	void moveSoundInFragment(String fragmentTag, int from, int to);

	/**
	 * Removes the given {@code MediaPlayerData} from the database of regular sound items.
	 * @param playerData {@code MediaPlayerData} to remove
	 */
	void removeSoundDataFromDatabase(MediaPlayerData playerData);

	/**
	 * Removes the given {@code MediaPlayerData} from the database of playlist items.
	 * @param playerData {@code MediaPlayerData} to remove
	 */
	void removePlaylistDataFromDatabase(MediaPlayerData playerData);
}
