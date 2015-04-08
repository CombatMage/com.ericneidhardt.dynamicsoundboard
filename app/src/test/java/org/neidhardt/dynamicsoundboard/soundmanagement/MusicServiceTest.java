package org.neidhardt.dynamicsoundboard.soundmanagement;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;

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
}