package org.neidhardt.dynamicsoundboard.soundmanagement.model

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundsDataUtil
{
	/**
	 * Starts async task to retrieve sounds from database. If `SoundsDataUtil` was initialized before,
	 * nothing is done.
	 */
	public fun initIfRequired()

	/**
	 * Check if `SoundsDataUtil.initIfRequired()` was called.
	 * @return true if `SoundsDataUtil` is initIfRequired, else false.
	 */
	public fun isInit(): Boolean

	/**
	 * Release all existing `EnhancedMediaPlayer` loaded in sounds and playlist.
	 */
	public fun release()

	/**
	 * Check if the given sound is part of the playlist or part of the regular sounds
	 * @param playerData data of sound to check
	 * *
	 * @return true if player data corresponds to playlist player else false
	 */
	public fun isPlaylistPlayer(playerData: MediaPlayerData): Boolean
}
