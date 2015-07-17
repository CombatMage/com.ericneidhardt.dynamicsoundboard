package org.neidhardt.dynamicsoundboard.mediaplayer.events;

import android.support.annotation.NonNull;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * File created by eric.neidhardt on 11.02.2015.
 */
public class MediaPlayerCompletedEvent
{
	private final EnhancedMediaPlayer player;

	public MediaPlayerCompletedEvent(@NonNull EnhancedMediaPlayer player)
	{
		this.player = player;
	}

	@NonNull
	public EnhancedMediaPlayer getPlayer()
	{
		return player;
	}
}
