package org.neidhardt.dynamicsoundboard.soundcontrol.events;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 29.05.2015.
 */
public class OpenSoundRenameEvent
{
	private final MediaPlayerData data;

	public OpenSoundRenameEvent(MediaPlayerData data) {
		this.data = data;
	}

	public MediaPlayerData getData()
	{
		return data;
	}
}
