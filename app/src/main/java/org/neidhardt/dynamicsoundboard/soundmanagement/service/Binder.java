package org.neidhardt.dynamicsoundboard.soundmanagement.service;

import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.MusicService;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
public class Binder extends android.os.Binder
{
	private MediaPlayerService service;

	public Binder(MediaPlayerService service)
	{
		this.service = service;
	}

	public MediaPlayerService getService()
	{
		return this.service;
	}
}
