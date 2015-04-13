package org.neidhardt.dynamicsoundboard.soundmanagement;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsLoadedEvent;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
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

		MediaPlayerData playerData = TestDataGenerator.getRandomPlayerData();
		this.service.onEventMainThread(new SoundsLoadedEvent(playerData));
		assertThat(this.service.getSounds().get(playerData.getFragmentTag()).size(), equalTo(1));
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

		this.service.onEventMainThread(new PlayListLoadedEvent(TestDataGenerator.getRandomPlayerData()));
		assertThat(this.service.getPlaylist().size(), equalTo(1));
	}

	@Test
	public void testAddNewSoundToServiceAndDatabase() throws Exception
	{
		// TODO
	}

	@Test
	public void testAddNewSoundToPlaylistAndDatabase() throws Exception
	{
		// TODO
	}
}