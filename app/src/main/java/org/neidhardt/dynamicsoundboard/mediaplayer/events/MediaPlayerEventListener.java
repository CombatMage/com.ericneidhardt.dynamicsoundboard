package org.neidhardt.dynamicsoundboard.mediaplayer.events;

import android.support.annotation.NonNull;

/**
 * File created by eric.neidhardt on 29.05.2015.
 */
public interface MediaPlayerEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a MediaPlayer changed his state (ie. start or stops playing).
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	void onEvent(@NonNull MediaPlayerStateChangedEvent event);

	/**
	 * This is called by greenRobot EventBus in case a MediaPlayer has finished playing.
	 * @param event delivered MediaPlayerStateChangedEvent
	 */
	void onEvent(@NonNull MediaPlayerCompletedEvent event);
}
