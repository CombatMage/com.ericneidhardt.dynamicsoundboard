package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

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
}
