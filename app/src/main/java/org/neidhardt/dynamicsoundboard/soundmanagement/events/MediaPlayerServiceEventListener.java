package org.neidhardt.dynamicsoundboard.soundmanagement.events;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public interface MediaPlayerServiceEventListener
{
	void onEvent(RequestInitEvent event);

	void onEvent(RequestWriteCachBackEvent event);

	void onEvent(RequestMoveSoundEvent event);

	void onEvent(RequestRemoveSoundsEvent event);

	void onEvent(RequestToggleSoundInPlaylistEvent event);
}
