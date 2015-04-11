package org.neidhardt.dynamicsoundboard.soundmanagement;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsLoadedEvent;

import static org.junit.Assert.assertTrue;

/**
 * Created by eric.neidhardt on 08.04.2015.
 */
public class MusicServiceTest extends ActivityTest
{
	@Test
	public void testGetDatabaseNameSounds() throws Exception
	{
		assertTrue(MusicService.getDatabaseNameSounds().equals(MusicService.DB_SOUNDS_DEFAULT));
	}

	@Test
	public void testGetDatabaseNamePlayList() throws Exception
	{
		assertTrue(MusicService.getDatabaseNamePlayList().equals(MusicService.DB_SOUNDS_PLAYLIST_DEFAULT));
	}

	@Test
	public void testOnSoundsLoadedEventMainThread() throws Exception
	{
		try
		{
			this.service.onEventMainThread(new SoundsLoadedEvent(null));
		}
		catch (NullPointerException e)
		{
			assertTrue("this should throw a NullPointerException exception and the list should remain empty", this.service.getSounds().isEmpty());
		}

		// TODO more testing
	}

	@Test
	public void testOnPlayListLoadedEventMainThread1() throws Exception
	{
		try
		{
			this.service.onEventMainThread(new PlayListLoadedEvent(null));
		}
		catch (NullPointerException e)
		{
			assertTrue("this should throw a NullPointerException exception and the list should remain empty", this.service.getPlaylist().isEmpty());
		}

		// TODO more testing
	}
}