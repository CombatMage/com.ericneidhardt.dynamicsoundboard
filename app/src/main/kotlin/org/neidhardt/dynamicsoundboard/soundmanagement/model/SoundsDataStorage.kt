package org.neidhardt.dynamicsoundboard.soundmanagement.model

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public interface SoundsDataStorage
{
	/**
	 * Create an new `EnhancedMediaPlayer` from given data and adds this player to the corresponding list of sounds.
	 * @param data `MediaPlayerData` to create player from
	 */
	fun createSoundAndAddToManager(data: MediaPlayerData)

	/**
	 * Create an new `EnhancedMediaPlayer` from given data and adds this player to the playlist.
	 * If creation failed, a new `CreatingPlayerFailedEvent` is posted.
	 * @param data `MediaPlayerData` to create player from
	 */
	fun createPlaylistSoundAndAddToManager(data: MediaPlayerData)

	/**
	 * Add or remove the sound from playlist.
	 * @param playerId id of player to toggle
	 * *
	 * @param addToPlaylist player's state in playlist
	 */
	fun toggleSoundInPlaylist(playerId: String, addToPlaylist: Boolean)

	/**
	 * Remove all given Sounds from the currently loaded sounds, but not from the playlist.
	 * @param soundsToRemove list of sounds to remove
	 */
	fun removeSounds(soundsToRemove: List<MediaPlayerController>)

	/**
	 * Remove all given Sounds from the currently loaded sounds, from the playlist.
	 * @param soundsToRemove list of sounds to remove
	 */
	fun removeSoundsFromPlaylist(soundsToRemove: List<MediaPlayerController>)

	/**
	 * Removes the given Sound from the list of currently playing sounds. The sound is not paused or stopped.
	 * The caller should take care, that the sound is no longer playing.
	 * @param soundToRemove sound to remove
	 */
	fun removeSoundFromCurrentlyPlayingSounds(soundToRemove: MediaPlayerController)

	/**
	 * Adds the given Sound to the list of currently playing sounds. The sound is not started.
	 * The caller should take care, that the sound is already playing.
	 * @param soundToAdd sound to add
	 */
	fun addSoundToCurrentlyPlayingSounds(soundToAdd: MediaPlayerController)

	/**
	 * Moves a certain sound in the list of sounds to another position
	 * @param fragmentTag TAG of SoundSheet
	 * *
	 * @param from position of moved sound
	 * *
	 * @param to new position of moved sound
	 */
	fun moveSoundInFragment(fragmentTag: String, from: Int, to: Int)

	/**
	 * Removes the given `MediaPlayerData` from the database of regular sound items.
	 * @param playerData `MediaPlayerData` to remove
	 */
	fun removeSoundDataFromDatabase(playerData: MediaPlayerData)

	/**
	 * Removes the given `MediaPlayerData` from the database of playlist items.
	 * @param playerData `MediaPlayerData` to remove
	 */
	fun removePlaylistDataFromDatabase(playerData: MediaPlayerData)

	/**
	 * Get DaoSession used for storing `MediaPlayerData` of regular sounds
	 * @return corresponding `DaoSession`
	 */
	fun getDbSounds(): DaoSession

	/**
	 * Get DaoSession used for storing `MediaPlayerData` of sounds in playlist
	 * @return corresponding `DaoSession`
	 */
	fun getDbPlaylist(): DaoSession
}
