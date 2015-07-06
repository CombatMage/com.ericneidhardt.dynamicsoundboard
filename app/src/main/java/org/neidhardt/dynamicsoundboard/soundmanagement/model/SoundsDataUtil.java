package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundsDataUtil
{
	/**
	 * Starts async task to retrieve sounds from database. If {@code SoundsDataUtil} was initialized before,
	 * you must call {@code SoundsDataUtil.release()} prior.
	 */
	void init();

	/**
	 * Check if {@code SoundsDataUtil.init()} was called.
	 * @return true if {@code SoundsDataUtil} is init, else false.
	 */
	boolean isInit();

	/**
	 * Release all existing {@code EnhancedMediaPlayer} loaded in sounds and playlist.
	 */
	void release();

	/**
	 * Check if the given sound is part of the playlist or part of the regular sounds
	 * @param playerData data of sound to check
	 * @return true if player data corresponds to playlist player else false
	 */
	boolean isPlaylistPlayer(MediaPlayerData playerData);
}
