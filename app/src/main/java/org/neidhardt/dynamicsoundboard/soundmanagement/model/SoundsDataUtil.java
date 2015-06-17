package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public interface SoundsDataUtil
{
	/**
	 * Starts async task to retrieve sounds from database. If {@code SoundsDataUtil} was initialized before,
	 * you must call {@code SoundsDataUtil.writeCacheBackAndRelease()} prior.
	 */
	void init();

	/**
	 * Check if {@code SoundsDataUtil.init()} was called.
	 * @return true if {@code SoundsDataUtil} is init, else false.
	 */
	boolean isInit();

	/**
	 * Write back all existing sounds and the playlist to database.
	 */
	void writeCacheBackAndRelease();

	/**
	 * Creates an new EnhancedMediaPlayer instance
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	EnhancedMediaPlayer createSound(MediaPlayerData playerData);

	/**
	 * Creates an new EnhancedMediaPlayer instance and adds this instance to the playlist.
	 * @param playerData raw data to create new MediaPlayer
	 * @return playerData to be stored in database, or null if creation failed
	 */
	EnhancedMediaPlayer createPlaylistSound(MediaPlayerData playerData);

}
