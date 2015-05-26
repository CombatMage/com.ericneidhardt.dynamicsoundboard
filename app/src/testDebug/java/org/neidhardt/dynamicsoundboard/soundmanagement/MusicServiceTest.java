package org.neidhardt.dynamicsoundboard.soundmanagement;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistLoadedEvent2;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundLoadedEvent;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by eric.neidhardt on 08.04.2015.
 */
public class MusicServiceTest extends AbstractBaseActivityTest
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
			this.service.onEvent(new SoundLoadedEvent(null, true));
		}
		catch (NullPointerException e)
		{
			assertTrue("this should throw a NullPointerException exception and the list should remain empty", this.service.getSounds().isEmpty());
		}

		MediaPlayerData playerData = TestDataGenerator.getRandomPlayerData();
		this.service.onEvent(new SoundLoadedEvent(playerData, true));
		assertThat(this.service.getSounds().get(playerData.getFragmentTag()).size(), equalTo(1));
	}

	@Test
	public void testOnPlayListLoadedEventMainThread1() throws Exception
	{
		try
		{
			this.service.onEvent(new PlaylistLoadedEvent2(null, true));
		}
		catch (NullPointerException e)
		{
			assertTrue("this should throw a NullPointerException exception and the list should remain empty", this.service.getPlaylist().isEmpty());
		}

		this.service.onEvent(new PlaylistLoadedEvent2(TestDataGenerator.getRandomPlayerData(), true));
		assertThat(this.service.getPlaylist().size(), equalTo(1));
	}

	@Test
	public void testOnMediaPlayerStateChangedEvent() throws Exception
	{
		EnhancedMediaPlayer testPlayer = TestDataGenerator.getMockEnhancedMediaPlayer(null);
		when(testPlayer.isPlaying()).thenReturn(true);

		this.service.onEvent(new MediaPlayerStateChangedEvent(testPlayer, true));
		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(1));

		when(testPlayer.isPlaying()).thenReturn(false);
		this.service.onEvent(new MediaPlayerStateChangedEvent(testPlayer, true));

		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(0));
	}
}